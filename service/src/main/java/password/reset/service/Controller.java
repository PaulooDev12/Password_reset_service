package password.reset.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import password.reset.service.annotaions.RateLimit;

import java.util.List;

@RestController
@RequestMapping("/")
public class Controller {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
       userRepository.save(user);
       return ResponseEntity.ok().body("user registered successfully");
    }

    @PostMapping("generate-token")
    @RateLimit(maxRequests = 3, windowInSeconds = 10)
    public ResponseEntity<String> generateToken(@RequestParam String username) {
        passwordResetService.generateResetToken(username);
        return ResponseEntity.ok().body("Se o username existir em nossa base enviaremos instruções");
    }
    @PostMapping("reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String password) {
        passwordResetService.ResetPassword(token, password);
        return ResponseEntity.ok("Senha alterada com sucesso");

    }
    @GetMapping("tests")
    @RateLimit(maxRequests = 3, windowInSeconds = 10)
    public ResponseEntity<List<User>> tests() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
