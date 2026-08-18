package password.reset.service.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import password.reset.service.annotaions.RateLimit;
import password.reset.service.exceptions.TooManyRequestException;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String ip = getClientIp(request);
        String methodName = joinPoint.getSignature().getName();
        String redisKey = "rateLimit:" + ip + ":" + methodName;
        Long currentRequests = redisTemplate.opsForValue().increment(redisKey);
        if(currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(redisKey, rateLimit.windowInSeconds(), TimeUnit.SECONDS);
        }
        if(currentRequests != null && currentRequests > rateLimit.maxRequests()) {
            throw new TooManyRequestException("Too Many Requests");
        }
        return joinPoint.proceed();
    }
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("x-forwarded-for");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
