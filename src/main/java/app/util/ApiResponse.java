package app.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    public ApiResponse(boolean successful, String message, T user) {
    }
}