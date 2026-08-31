package pl.ib.beauty.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.ib.beauty.mapper.TicketMapper;
import pl.ib.beauty.model.dao.*;
import pl.ib.beauty.model.dto.*;
import pl.ib.beauty.repository.TicketRepository;
import pl.ib.beauty.repository.UserRepository;
import pl.ib.beauty.security.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;

    @Transactional
    public TicketDto createTicket(TicketCreateDto dto) {
        User author = currentUser();
        Ticket ticket = Ticket.builder()
                .title(dto.title())
                .description(dto.description())
                .type(dto.type())
                .status(TicketStatus.OPEN)
                .author(author)
                .build();
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getMyTickets() {
        User user = currentUser();
        return ticketRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId())
                .stream().map(ticketMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketDto> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(ticketMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TicketDto getById(Long id) {
        Ticket ticket = findTicket(id);
        assertAccess(ticket);
        return ticketMapper.toDto(ticket);
    }

    @Transactional
    public TicketDto updateStatus(Long id, TicketStatusUpdateDto dto) {
        Ticket ticket = findTicket(id);
        ticket.setStatus(dto.status());
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto addReply(Long id, AddReplyDto dto) {
        Ticket ticket = findTicket(id);
        assertAccess(ticket);
        User user = currentUser();
        boolean isSupport = SecurityUtils.hasRole("SCOPE_SUPPORT");

        TicketReply reply = TicketReply.builder()
                .id(UUID.randomUUID().toString())
                .authorId(user.getId())
                .authorName(user.getFirstName() + " " + user.getLastName())
                .authorRole(isSupport ? "SUPPORT" : "USER")
                .content(dto.content())
                .aiGenerated(false)
                .createdAt(LocalDateTime.now().toString())
                .build();

        ticket.getReplies().add(reply);

        if (isSupport && ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto resolveTicket(Long id, ResolveTicketDto dto) {
        Ticket ticket = findTicket(id);
        User user = currentUser();

        TicketResolution resolution = TicketResolution.builder()
                .source("SUPPORT")
                .content(dto.content())
                .resolvedById(user.getId())
                .resolvedByName(user.getFirstName() + " " + user.getLastName())
                .resolvedAt(LocalDateTime.now().toString())
                .build();

        ticket.setResolution(resolution);
        ticket.setStatus(TicketStatus.RESOLVED);
        return ticketMapper.toDto(ticketRepository.save(ticket));
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found: " + id));
    }

    private User currentUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private void assertAccess(Ticket ticket) {
        boolean isSupport = SecurityUtils.hasRole("SCOPE_SUPPORT");
        if (isSupport) return;
        String email = SecurityUtils.getCurrentUserEmail();
        if (!ticket.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("Access denied to ticket: " + ticket.getId());
        }
    }
}
