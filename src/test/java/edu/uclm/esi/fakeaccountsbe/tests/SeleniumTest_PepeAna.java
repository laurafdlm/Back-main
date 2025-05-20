package edu.uclm.esi.fakeaccountsbe.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
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
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\LauraFernandez\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\LauraFernandez\\Downloads\\chrome-win64\\chrome-win64\\chrome.exe");
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
            String nombreLista = "Cumpleaños " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            // Login Pepe
            driverPepe.get("http://localhost:4200/login");
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("annafdlm4@gmail.com");
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys("12345");
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]"))).click();
            waitPepe.until(ExpectedConditions.urlContains("/home"));

            // Crear lista
            driverPepe.get("http://localhost:4200/lists");
            waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Nueva lista']"))).sendKeys(nombreLista);
            waitPepe.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Añadir')]"))).click();
            WebElement nuevaLista = waitPepe.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//strong[contains(text(),'" + nombreLista + "')]")));
            WebElement listaItem = nuevaLista.findElement(By.xpath("./ancestor::li"));
            String idLista = listaItem.getAttribute("id");

            // Ir a productos y añadir
            driverPepe.get("http://localhost:4200/lists/" + idLista + "/products?owner=true");
            waitPepe.until(ExpectedConditions.urlContains("/products"));
            aniadirProducto(driverPepe, waitPepe, "Cerveza", "30");
            aniadirProducto(driverPepe, waitPepe, "Tarta", "1");
            aniadirProducto(driverPepe, waitPepe, "Patatas fritas", "2");

            // Obtener token actual de Pepe desde localStorage (opcional si no lo haces manualmente en el test)
            String tokenPepe = ((JavascriptExecutor) driverPepe)
                .executeScript("return window.localStorage.getItem('token');").toString();

            // Hacer POST a /listas/compartirLista y obtener el sharedUrl
            String sharedUrl = obtenerSharedUrlDesdeBackend(idLista, "ana@gmail.com", tokenPepe);
            System.out.println("Invitación generada: " + sharedUrl);

            // Login Ana
            driverAna.get("http://localhost:4200/login");
            waitAna.until(ExpectedConditions.presenceOfElementLocated(By.id("email"))).sendKeys("ana@gmail.com");
            waitAna.until(ExpectedConditions.presenceOfElementLocated(By.id("password"))).sendKeys("1234");
            waitAna.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]"))).click();
            waitAna.until(ExpectedConditions.urlContains("/home"));

            // Acceder directamente al enlace de invitación para que acepte
            driverAna.get(sharedUrl);
            waitAna.until(ExpectedConditions.urlContains("/invitation-accepted"));

            // Ir a listas compartidas y verificar que aparece
            driverAna.get("http://localhost:4200/lists");
            WebElement lista = waitAna.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//li[contains(.,'" + nombreLista + "')]")));
            WebElement btnVerProductos = lista.findElement(By.xpath(".//button[contains(text(),'Ver Productos')]"));
            ((JavascriptExecutor) driverAna).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnVerProductos);
            Thread.sleep(300); // evitar que la animación lo tape
            ((JavascriptExecutor) driverAna).executeScript("arguments[0].click();", btnVerProductos);

            waitAna.until(ExpectedConditions.urlContains("/products"));

            // Ana compra la tarta
            List<WebElement> productos = waitAna.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("ul.list-group > li")));
            for (WebElement producto : productos) {
                if (producto.getText().contains("Tarta")) {
                    WebElement input = producto.findElement(By.cssSelector("input[placeholder='Cantidad']"));
                    input.clear();
                    input.sendKeys("1");
                    WebElement btnComprar = producto.findElement(By.xpath(".//button[contains(text(),'Comprar')]"));

                 // Scroll hasta el botón para asegurar que no está tapado
                 ((JavascriptExecutor) driverAna).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnComprar);
                 Thread.sleep(300); // Espera un momento para evitar que esté animándose
                 ((JavascriptExecutor) driverAna).executeScript("arguments[0].click();", btnComprar);

                    break;
                }
            }

            // Verificación en Pepe
            Thread.sleep(2000);
            driverPepe.navigate().refresh();
            driverPepe.get("http://localhost:4200/lists/" + idLista + "/products?owner=true");
            WebElement listaProductosPepe = waitPepe.until(ExpectedConditions.presenceOfElementLocated(By.className("list-group")));
            boolean tartaComprada = listaProductosPepe.getText().contains("Tarta") && listaProductosPepe.getText().contains("1/1");

            if (!tartaComprada)
                fail("❌ La tarta no aparece como comprada en el navegador de Pepe.");

            System.out.println("✔ Todo el flujo ejecutado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            fail("❌ Error en el flujo completo de prueba.");
        }
    }
    private String obtenerSharedUrlDesdeBackend(String idLista, String emailInvitado, String token) throws Exception {
        URL url = new URL("http://localhost:80/listas/compartirLista");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", token);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String jsonInput = String.format("{\"idLista\":\"%s\",\"email\":\"%s\"}", idLista, emailInvitado);
        try (java.io.OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        if (code != 200) throw new RuntimeException("Fallo al compartir lista, código: " + code);

        String response;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream(), "utf-8"))) {
            response = br.lines().reduce("", (acc, line) -> acc + line);
        }

        // Extraer sharedUrl del JSON
        int start = response.indexOf("sharedUrl\":\"") + 12;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    // Añadir producto
    private void aniadirProducto(WebDriver driver, WebDriverWait wait, String nombre, String cantidad) throws InterruptedException {
        WebElement inputNombre = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("newProductName")));
        WebElement inputCantidad = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("newProductQuantity")));
        inputNombre.clear();
        inputNombre.sendKeys(nombre);
        inputNombre.sendKeys(Keys.TAB);
        inputCantidad.clear();
        inputCantidad.sendKeys(cantidad);
        inputCantidad.sendKeys(Keys.TAB);
        Thread.sleep(500);
        driver.findElement(By.xpath("//button[contains(text(),'Añadir Producto')]")).click();
        Thread.sleep(1000);
    }
}
