package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestCrearListaYAgregarProducto {

    private WebDriver driver;
    private WebDriverWait wait;
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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
    public void testCrearListaYAgregarProducto() {
        driver.get("http://localhost:4200");
        driver.manage().window().setSize(new Dimension(1200, 800));

        // Login
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Iniciar Sesión")));
        loginBtn.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("laurafernandezdm@gmail.com");
        driver.findElement(By.id("password")).sendKeys("1234");
        driver.findElement(By.xpath("//button[contains(text(),'Iniciar Sesión')]")).click();

        // Ir a "Gestionar listas"
        WebElement gestionarBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Gestionar Listas')]")));
        gestionarBtn.click();

        // Crear nueva lista
        String nombreLista = "Lista Selenium " + System.currentTimeMillis();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Nueva lista']")))
            .sendKeys(nombreLista);
        driver.findElement(By.xpath("//button[contains(text(),'Añadir')]")).click();

        // Esperar a que aparezca la nueva lista en alguno de los elementos
        wait.until(driver -> {
            List<WebElement> items = driver.findElements(By.className("list-group-item"));
            return items.stream().anyMatch(e -> e.getText().contains(nombreLista));
        });

        // Ver productos de esa lista
        WebElement botonVerProductos = driver.findElement(By.xpath("//button[contains(text(),'Ver Productos')]"));
        botonVerProductos.click();

        // Añadir producto
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("newProductName"))).sendKeys("Manzanas");
        driver.findElement(By.id("newProductQuantity")).sendKeys("3");
        driver.findElement(By.xpath("//button[contains(text(),'Añadir Producto')]")).click();

        // Verificar que aparece en la lista
        WebElement listaProductos = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("list-group")));
        assertTrue(listaProductos.getText().contains("Manzanas"));
        assertTrue(listaProductos.getText().contains("3"));
    }
}
