package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestLoginUsuario {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver",
            "C:\\Users\\LauraFernandez\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\LauraFernandez\\Downloads\\chrome-win64\\chrome-win64\\chrome.exe");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testLoginUsuario() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get("http://localhost:4200");
        driver.manage().window().setSize(new Dimension(1200, 800));

        // Ir a login si no estamos ahí
        if (!driver.getCurrentUrl().contains("/login")) {
            WebElement btnLogin = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Iniciar Sesión")));
            btnLogin.click();
        }

        // Introducir credenciales
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("testselenium@uclm.com");
        driver.findElement(By.id("password")).sendKeys("Test1234");

        // Clic en botón de inicio de sesión
        driver.findElement(By.xpath("//button[contains(text(),'Iniciar Sesión')]")).click();

        // Esperar a que redirija a /home
        wait.until(ExpectedConditions.urlContains("/home"));

        // Verificar que se está en home
        assertTrue(driver.getCurrentUrl().contains("/home"), "No se redirigió al home tras iniciar sesión.");
    }
}
