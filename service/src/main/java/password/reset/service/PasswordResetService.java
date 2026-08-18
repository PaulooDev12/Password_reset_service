package password.reset.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;
import java.security.SecureRandom;
@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final SecureRandom secureRandom = new SecureRandom();

    private String generateVerificationCode(){
        int number = secureRandom.nextInt(1000000);
        return String.format("%06d", number);
    }

    public void generateResetToken(String username) {

        String token = generateVerificationCode();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                    new RuntimeException("Usuario não encontrado")
                );
        System.out.println("Seu token de reset: " + token);
        stringRedisTemplate.opsForValue().set(
                token,
                user.getId().toString(),
                15,
                TimeUnit.MINUTES);
    }
    public void ResetPassword(String token, String newPassword) {
        String userId = stringRedisTemplate.opsForValue().getAndDelete(token);
        if(userId == null) {
            throw new RuntimeException("usuario não encontrado");
        }
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException(""));
        user.setPassword(newPassword);
        userRepository.save(user);
    }
}
