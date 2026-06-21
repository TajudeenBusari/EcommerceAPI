package userutils.dto;

public record LoginResponseDto (
        UserDto userInfo,
        String token
) {
}
