package edu.uclm.esi.fakeaccountsbe.http;

import java.io.IOException;

import java.sql.Timestamp;
import java.util.Collection;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.RequestHeader;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;

import org.springframework.http.ResponseEntity;

import edu.uclm.esi.fakeaccountsbe.model.CredencialesRegistro;
import edu.uclm.esi.fakeaccountsbe.model.PaymentRequest;
import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.repositories.UserRepository;
import edu.uclm.esi.fakeaccountsbe.services.UserService;


@RestController
@RequestMapping("users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Value("${stripe.secret.key:#{systemEnvironment['STRIPE_SECRET_KEY']}}")
	private String stripeSecretKey;

	@PostMapping("/registrar1")
	public ResponseEntity<Map<String, String>> registrar1(HttpServletRequest req, @RequestBody CredencialesRegistro cr) {
	    // Validar credenciales
	    cr.comprobar();

	    // Crear un nuevo usuario
	    User user = new User();
	    user.setEmail(cr.getEmail());
	    user.setPwd(cr.getPwd1());

	    // Establecer la dirección IP del usuario
	    user.setIp(req.getRemoteAddr());

	    // Establecer una fecha de expiración para el token (1 hora a partir del registro)
	    user.setTokenExpiry(System.currentTimeMillis() + 3600000); // El token expira en 1 hora

	    // Registrar el usuario en la base de datos
	    this.userService.registrar(req.getRemoteAddr(), user);

	    // Generar un token único para el usuario registrado
	    String token = UUID.randomUUID().toString();
	    user.setToken(token);

	    // Guardar el usuario con el token en la base de datos
	    this.userService.save(user);

	    // Preparar la respuesta con el token
	    Map<String, String> response = new HashMap<>();
	    response.put("message", "Usuario registrado con éxito.");
	    response.put("token", token);
	    return ResponseEntity.ok(response);
	}



    
    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Correo electrónico es obligatorio");
        }
        // Lógica para generar y enviar el correo con el enlace de recuperación
        boolean success = userService.sendRecoveryEmail(email);
        if (success) {
            return ResponseEntity.ok("Correo enviado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario no encontrado.");
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Correo electrónico es obligatorio");
            return ResponseEntity.badRequest().body(response);
        }
        boolean result = userService.sendRecoveryEmail(email);

        if (result) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Correo enviado correctamente.");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario no encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    
	@GetMapping("/registrar2")
	public void registrar2(HttpServletRequest req, @RequestParam String email, @RequestParam String pwd1, @RequestParam String pwd2) {
		CredencialesRegistro cr = new CredencialesRegistro();
		cr.setEmail(email);
		cr.setPwd1(pwd1);
		cr.setPwd2(pwd2);
		cr.comprobar();
		User user = new User();
		user.setEmail(cr.getEmail());
		user.setPwd(cr.getPwd1());
		
		this.userService.registrar(req.getRemoteAddr(), user);
	}
	
	@GetMapping("/validate-token")
	public ResponseEntity<?> validateToken(@RequestParam String token) {
	    Optional<User> user = userRepository.findByToken(token);
	    if (!user.isPresent() || userService.isTokenExpired(user.get())) { // Validar si el token ha expirado
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado.");
	    }
	    Map<String, String> response = new HashMap<>();
	    response.put("message", "Token válido.");
	    return ResponseEntity.ok(response);
	}



	
	@GetMapping("/registrarMuchos")
	public void registrarMuchos(HttpServletRequest req, @RequestParam String name, @RequestParam Integer n) {
		for (int i=0; i<n; i++)
			this.registrar2(req, name + i + "@pepe.com", "Pepe1234", "Pepe1234");
	}
	
	@PutMapping("/login1")
	public ResponseEntity<Map<String, String>> login1(@RequestBody User user) {
	    User loggedUser = this.userService.find(user.getEmail(), user.getPwd());
	    loggedUser.setToken(UUID.randomUUID().toString());
	    this.userService.save(loggedUser); // Guarda el token actualizado en la base de datos

	    Map<String, String> response = new HashMap<>();
	    response.put("token", loggedUser.getToken());
	    return ResponseEntity.ok(response);
	}
	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(@RequestHeader("token") String token) {
	    User user = userService.findByToken(token);
	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o expirado.");
	    }
	    Map<String, Object> profile = new HashMap<>();
	    profile.put("email", user.getEmail());
	    profile.put("isPaid", user.getIsPaid());
	    return ResponseEntity.ok(profile);
	}


	@PutMapping("/profile/update")
	public ResponseEntity<?> updateProfile(@RequestHeader("token") String token, @RequestBody Map<String, String> updates) {
	    User user = userService.findByToken(token);
	    if (user == null) {
	        return ResponseEntity.status(401).body("Usuario no autenticado.");
	    }

	    if (updates.containsKey("email")) {
	        user.setEmail(updates.get("email"));
	    }

	    if (updates.containsKey("pwd")) {
	        user.setPwd(updates.get("pwd"));
	    }

	    userService.save(user);
	    return ResponseEntity.ok("Perfil actualizado con éxito.");
	}




	
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
	    String token = request.get("token");
	    String newPassword = request.get("password");

	    if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
	        return ResponseEntity.badRequest().body("Token o contraseña faltante.");
	    }

	    Optional<User> optionalUser = userRepository.findByToken(token);
	    if (!optionalUser.isPresent() || userService.isTokenExpired(optionalUser.get())) { // Validar si el token ha expirado
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido o expirado.");
	    }

	    User user = optionalUser.get();
	    user.setPwd(newPassword);
	    user.setToken(null); // Invalida el token
	    user.setTokenExpiry(null); // Limpia la expiración del token
	    userService.save(user);

	    Map<String, String> response = new HashMap<>();
	    response.put("message", "Contraseña restablecida con éxito.");
	    return ResponseEntity.ok(response);
	}




	@GetMapping("/reset-password")
	public void redirectToAngular(HttpServletResponse response) throws IOException {
	    response.sendRedirect("http://localhost:4200/reset-password");
	}


	@PostMapping("/pay")
	public ResponseEntity<String> pay(@RequestHeader("token") String token) {
	    User user = userService.findByToken(token); // Llama al método findByToken
	    user.setIsPaid(true); // Marca al usuario como pagado
	    userService.save(user); // Guarda los cambios en la base de datos
	    return ResponseEntity.ok("Pago realizado con éxito.");
	}

	@PostMapping("/payment")
	public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
	    Stripe.apiKey = stripeSecretKey;
	    try {
	        // Crear un PaymentIntent con Stripe
	        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
	            .setAmount((long) request.getAmount())
	            .setCurrency("usd")
	            .setPaymentMethod(request.getPaymentMethodId())
	            .setConfirm(true)
	            .build();

	        PaymentIntent intent = PaymentIntent.create(params);
	        System.out.println("PaymentIntent creado: " + intent.getId());

	        // Buscar al usuario por email
	        User user = userService.findByEmail(request.getEmail());
	        if (user == null) {
	            System.out.println("Usuario no encontrado: " + request.getEmail());
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
	        }

	        // Marcar al usuario como pagado
	        user.setIsPaid(true);
	        userService.save(user);
	        System.out.println("Usuario actualizado: " + user.getEmail() + ", isPaid: " + user.getIsPaid());

	        return ResponseEntity.ok("Pago exitoso");
	    } catch (StripeException e) {
	        System.err.println("Error en el pago: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error en el pago: " + e.getMessage());
	    }
	}







	@GetMapping("/payment-status")
	public ResponseEntity<?> getPaymentStatus(@RequestHeader("token") String token) {
	    User user = userService.findByToken(token);
	    if (user == null) {
	        return ResponseEntity.status(401).body("Usuario no autenticado.");
	    }

	    return ResponseEntity.ok(user.getIsPaid());
	}


	@GetMapping("/login2")
	public User login2(HttpServletResponse response, @RequestParam String email, @RequestParam String pwd) {
		User user = this.userService.find(email, pwd);
		user.setToken(UUID.randomUUID().toString());
		response.setHeader("token", user.getToken());
		return user;
	}
	
	@GetMapping("/login3/{email}")
	public User login3(HttpServletResponse response, @PathVariable String email, @RequestParam String pwd) {
		return this.login2(response, email, pwd);
	}
	
	@GetMapping("/getAllUsers")
	public Collection<User> getAllUsers() {
		return this.userService.getAllUsers();
	}
	
	@DeleteMapping("/delete-account")
	public ResponseEntity<?> deleteAccount(@RequestHeader("token") String token) {
	    User user = this.userService.findByToken(token);
	    if (user == null) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
	    }
	    this.userService.delete(user.getEmail()); // Pasar el email
	    return ResponseEntity.ok("Cuenta eliminada con éxito.");
	}


	@PutMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestHeader("token") String token, @RequestBody Map<String, String> body) {
	    User user = userService.findByToken(token);
	    if (user == null) {
	        return ResponseEntity.status(401).body("Usuario no autenticado.");
	    }

	    String currentPassword = body.get("currentPassword");
	    String newPassword = body.get("newPassword");

	    if (!user.getPwd().equals(currentPassword)) {
	        return ResponseEntity.status(400).body("La contraseña actual es incorrecta.");
	    }

	    user.setPwd(newPassword);
	    userService.save(user);

	    return ResponseEntity.ok("Contraseña actualizada con éxito.");
	}


	@DeleteMapping("/clearAll")
	public void clearAll(HttpServletRequest request) {
		String sToken = request.getHeader("prime");
		Integer token = Integer.parseInt(sToken);
		if (!isPrime(token.intValue()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes pasar un número primo en la cabecera");
		if (sToken.length()!=3)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El nº primo debe tener tres cifras");
		this.userService.clearAll();
	}
	
	private boolean isPrime(int n) {
	    if (n <= 1) return false;
	    for (int i = 2; i <= Math.sqrt(n); i++) {
	        if (n % i == 0) return false;
	    }
	    return true;
	}
}
















