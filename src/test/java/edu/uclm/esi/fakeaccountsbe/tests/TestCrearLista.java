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
public class TestCrearLista {

    private WebDriver driver;
    private Map<String, Object> vars;
    JavascriptExecutor js;

    @BeforeEach
    public void setUp() {
        System.setProperty("webdriver.chrome.driver",
            "C:\\\\Users\\\\LauraFernandez\\\\Downloads\\\\chromedriver-win64\\\\chromedriver-win64\\\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\\\Users\\\\LauraFernandez\\\\Downloads\\\\chrome-win64\\\\chrome-win64\\\\chrome.exe");
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
    public void testCrearLista() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get("http://localhost:4200");
        driver.manage().window().setSize(new Dimension(1200, 800));

        // Ir a login
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Iniciar Sesión"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("testselenium@uclm.com");
        driver.findElement(By.id("password")).sendKeys("Test1234");
        driver.findElement(By.xpath("//button[contains(text(),'Iniciar Sesión')]")).click();

        // Ir a /lists
        wait.until(ExpectedConditions.urlContains("/home"));
        driver.findElement(By.xpath("//button[contains(text(),'Gestionar Listas')]")).click();
        wait.until(ExpectedConditions.urlContains("/lists"));

        // Crear lista
        String nombreLista = "Lista Selenium " + System.currentTimeMillis();
        WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.form-control")));
        input.sendKeys(nombreLista);
        driver.findElement(By.xpath("//button[contains(text(),'Añadir')]")).click();

        // Verificar que aparece en pantalla
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//strong[text()='" + nombreLista + "']")));

        assertTrue(driver.findElement(By.xpath("//strong[text()='" + nombreLista + "']")).isDisplayed(),
                   "La lista no fue creada correctamente.");
    }
}