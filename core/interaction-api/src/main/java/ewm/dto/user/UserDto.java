package ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;

    @NotBlank
    @Size(min = 2, message = "{validation.name.size.too_short}")
    @Size(max = 250, message = "{validation.name.size.too_long}")
    private String name;

    @NotBlank
    @Size(min = 6, message = "{validation.email.size.too_short}")
    @Size(max = 254, message = "{validation.email.size.too_long}")
    @Email(regexp = ".+[@].+[\\.].+")
    private String email;
}
