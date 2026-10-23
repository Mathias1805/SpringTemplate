package Template.Api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class TemplateApplicationTests {

    // JavaMailSender no está disponible en tests (no hay servidor SMTP).
    // Se mockea para que el contexto de Spring pueda arrancar sin infra de email.
    @MockBean
    JavaMailSender mailSender;

    @Test
    void contextLoads() {
    }

}
