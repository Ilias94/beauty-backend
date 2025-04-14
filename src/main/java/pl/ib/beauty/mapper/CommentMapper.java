package pl.ib.beauty.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.ib.beauty.model.dao.Comment;
import pl.ib.beauty.model.dao.User;
import pl.ib.beauty.model.dto.CommentDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(source = "author", target = "authorName", qualifiedByName = "mapAuthorName")
    CommentDto commentToCommentDto(Comment comment);

    List<CommentDto> commentsToCommentsDto(List<Comment> comments);

    @Named("mapAuthorName")
    default String mapAuthorName(User author) {
        return "%s %s.".formatted(author.getFirstName(), author.getLastName().charAt(0));
    }
}
