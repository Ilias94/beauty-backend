package pl.ib.beauty.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ib.beauty.model.dto.*;
import pl.ib.beauty.service.TicketService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/tickets", produces = MediaType.APPLICATION_JSON_VALUE)
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public TicketDto createTicket(@RequestBody @Valid TicketCreateDto dto) {
        return ticketService.createTicket(dto);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<TicketDto> getMyTickets() {
        return ticketService.getMyTickets();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_SUPPORT')")
    public List<TicketDto> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TicketDto getById(@PathVariable Long id) {
        return ticketService.getById(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SCOPE_SUPPORT')")
    public TicketDto updateStatus(@PathVariable Long id, @RequestBody @Valid TicketStatusUpdateDto dto) {
        return ticketService.updateStatus(id, dto);
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("isAuthenticated()")
    public TicketDto addReply(@PathVariable Long id, @RequestBody @Valid AddReplyDto dto) {
        return ticketService.addReply(id, dto);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('SCOPE_SUPPORT')")
    public TicketDto resolveTicket(@PathVariable Long id, @RequestBody @Valid ResolveTicketDto dto) {
        return ticketService.resolveTicket(id, dto);
    }
}
