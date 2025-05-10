package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestRegistroLogin {

    private WebDriver driver;
    private Map<String, Object> vars;
    JavascriptExecutor js;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver",
            "C:\\Users\\LauraFernandez\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\LauraFernandez\\Downloads\\chrome-win64\\chrome-win64\\chrome.exe");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver;
        vars = new HashMap<>();
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testRegistroUsuario() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get("http://localhost:4200");
        driver.manage().window().setSize(new Dimension(1200, 800));

        // Hacer clic en el enlace "Registro"
        WebElement btnRegistro = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Registro")));
        btnRegistro.click();

        // Completar formulario
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("selenium" + System.currentTimeMillis() + "@uclm.com");
        driver.findElement(By.id("pwd1")).sendKeys("Test1234");
        driver.findElement(By.id("pwd2")).sendKeys("Test1234");

        // Click en el botón Registrar
        driver.findElement(By.xpath("//button[contains(text(),'Registrar')]")).click();

        // Esperar a que se redirija a /home
        wait.until(ExpectedConditions.urlContains("/home"));

        // Comprobar que está en /home
        assertTrue(driver.getCurrentUrl().contains("/home"), "No se redirigió correctamente al home tras registrarse.");
    }
}
