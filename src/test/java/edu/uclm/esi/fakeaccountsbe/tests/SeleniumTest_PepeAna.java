package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeleniumTest_PepeAna {

    private WebDriver driverPepe;
    private WebDriver driverAna;
    private WebDriverWait waitPepe;
    private WebDriverWait waitAna;

    @BeforeAll
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\\\Users\\\\LauraFernandez\\\\Downloads\\\\chromedriver-win64\\\\chromedriver-win64\\\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\\\Users\\\\LauraFernandez\\\\Downloads\\\\chrome-win64\\\\chrome-win64\\\\chrome.exe");
        options.addArguments("--remote-allow-origins=*");

        driverPepe = new ChromeDriver(options);
        driverAna = new ChromeDriver(options);

        waitPepe = new WebDriverWait(driverPepe, Duration.ofSeconds(20));
        waitAna = new WebDriverWait(driverAna, Duration.ofSeconds(20));

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int halfWidth = screenWidth / 2;

        driverPepe.manage().window().setSize(new Dimension(halfWidth, 800));
        driverPepe.manage().window().setPosition(new Point(0, 0));

        driverAna.manage().window().setSize(new Dimension(halfWidth, 800));
        driverAna.manage().window().setPosition(new Point(halfWidth, 0));
    }

    @AfterAll
    public void tearDown() {
        if (driverPepe != null) driverPepe.quit();
        if (driverAna != null) driverAna.quit();
    }

    @Test
    public void testFlujoCompleto() {
        try {
            String nombreLista = "Cumpleaños " + System.currentTimeMillis();

            // Login Pepe
            driverPepe.get("http://localhost:4200/login");
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("annafdlm4@gmail.com");
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys("12345");
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]"))).click();
            waitPepe.until(ExpectedConditions.urlContains("/home"));

            // Crear lista
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Gestionar Listas')]"))).click();
            waitPepe.until(ExpectedConditions.urlContains("/lists"));
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Nueva lista']"))).sendKeys(nombreLista);
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Añadir')]"))).click();
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//strong[contains(text(),'" + nombreLista + "')]")));

            // Ver productos
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Productos')]"))).click();
            waitPepe.until(ExpectedConditions.urlContains("/products"));

            // Añadir productos
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.id("newProductName"))).sendKeys("Cerveza");
            driverPepe.findElement(By.id("newProductQuantity")).sendKeys("30");
            driverPepe.findElement(By.xpath("//button[contains(text(),'Añadir Producto')]")).click();
            Thread.sleep(1000);

            driverPepe.findElement(By.id("newProductName")).sendKeys("Tarta");
            driverPepe.findElement(By.id("newProductQuantity")).sendKeys("1");
            driverPepe.findElement(By.xpath("//button[contains(text(),'Añadir Producto')]")).click();
            Thread.sleep(1000);

            driverPepe.findElement(By.id("newProductName")).sendKeys("Patatas fritas");
            driverPepe.findElement(By.id("newProductQuantity")).sendKeys("2");
            driverPepe.findElement(By.xpath("//button[contains(text(),'Añadir Producto')]")).click();
            Thread.sleep(1000);

            driverPepe.navigate().back();

            // Compartir con Ana
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Compartir')]"))).click();
            waitPepe.until(ExpectedConditions.visibilityOfElementLocated(By.id("shareModal")));
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Introduce el correo electrónico']"))).sendKeys("ana@gmail.com");
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Enviar Invitación')]"))).click();
            Thread.sleep(1500);

            // Login Ana
            driverAna.get("http://localhost:4200/login");
            waitAna.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("ana@gmail.com");
            waitAna.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys("1234");
            waitAna.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]"))).click();
            waitAna.until(ExpectedConditions.urlContains("/home"));

            // Ver lista compartida
            driverAna.get("http://localhost:4200/lists");
            waitAna.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//h3[contains(text(),'Listas compartidas')]/following::button[contains(text(),'Ver Productos')]")
            )).click();
            waitAna.until(ExpectedConditions.urlContains("/products"));

            // Comprar Tarta
            List<WebElement> productos = waitAna.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("ul.list-group > li")));
            boolean encontrada = false;

            for (WebElement producto : productos) {
                if (producto.getText().contains("Tarta")) {
                    WebElement inputCantidad = producto.findElement(By.cssSelector("input[placeholder='Cantidad']"));
                    inputCantidad.clear();
                    inputCantidad.sendKeys("1");

                    WebElement btnComprar = producto.findElement(By.xpath(".//button[contains(text(),'Comprar')]"));
                    btnComprar.click();
                    encontrada = true;
                    break;
                }
            }

            if (!encontrada) {
                fail("❌ No se encontró el producto 'Tarta' en la vista de Ana.");
            }

            // Esperar a que el WebSocket actualice la vista de Pepe
            Thread.sleep(2000);

            // Verificar en vista de Pepe
            driverPepe.navigate().refresh();
            waitPepe.until(ExpectedConditions.urlContains("/lists"));
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Ver Productos')]"))).click();
            waitPepe.until(ExpectedConditions.urlContains("/products"));

            WebElement listaProductosPepe = waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.className("list-group")));
            boolean tartaComprada = listaProductosPepe.getText().contains("Tarta") && listaProductosPepe.getText().contains("1/1");

            if (!tartaComprada) {
                fail("❌ La tarta no aparece como comprada en el navegador de Pepe.");
            }

            System.out.println("✔ Todo el flujo ejecutado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            fail("❌ Error en el flujo completo de prueba.");
        }
    }
}
