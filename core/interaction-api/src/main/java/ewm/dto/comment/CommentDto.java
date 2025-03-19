package ewm.dto.comment;

import ewm.dto.user.UserDto;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private UserDto author;
}
