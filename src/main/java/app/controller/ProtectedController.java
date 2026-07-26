package app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class ProtectedController {

    @GetMapping("/dashboard")
    public String adminSetting() {
        return "Access Granted: Welcome to the Secure Administration panel.";
    }
}
