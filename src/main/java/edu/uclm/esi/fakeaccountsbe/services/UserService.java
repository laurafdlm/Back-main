package edu.uclm.esi.fakeaccountsbe.services;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.esi.fakeaccountsbe.exceptions.UserAlreadyExistsException;
import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.repositories.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class UserService {
		
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    
	private Map<String, User> users = new ConcurrentHashMap<>();
	private Map<String, List<User>> usersByIp = new ConcurrentHashMap<>();

	public void registrar(String ip, User user) {
	    if (userRepository.existsByEmail(user.getEmail())) {
	        throw new UserAlreadyExistsException("Ya existe un usuario con ese correo electrónico");
	    }

	    user.setIp(ip);
	    user.setCreationTime(System.currentTimeMillis());
	    userRepository.save(user); // Guardar el usuario en la base de datos
	    System.out.println("Usuario registrado exitosamente: " + user.getEmail());
	}

	public void login(User tryingUser) {
		this.find(tryingUser.getEmail(), tryingUser.getPwd());
	}

	public void clearAll() {
		this.usersByIp.clear();
		this.users.clear();
	}

	public Collection<User> getAllUsers() {
		return this.users.values();
	}
	
	public boolean sendRecoveryEmail(String email) {
	    User user = userRepository.findByEmail(email).orElse(null); // Cambiar findById a findByEmail
	    if (user == null) {
	        return false;
	    }

	    String recoveryToken = java.util.UUID.randomUUID().toString();
	    user.setToken(recoveryToken);
	    userRepository.save(user);

	    String recoveryUrl = "http://localhost:4200/reset-password?token=" + recoveryToken;

	    try {
	        sendEmail(email, recoveryUrl);
	        return true;
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        return false;
	    }
	}



	private void sendEmail(String to, String recoveryUrl) throws MessagingException {
	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message, true);

	    helper.setTo(to);
	    helper.setSubject("Recuperación de contraseña");
	    helper.setText(
	        "<p>Para restablecer tu contraseña, haz clic en el siguiente enlace:</p>" +
	        "<a href='" + recoveryUrl + "'>Restablecer Contraseña</a>",
	        true
	    );

	    mailSender.send(message);
	}

	
	public User find(String email, String pwd) {
	    User user = userRepository.findByEmail(email).orElse(null); // Cambiar findById a findByEmail
	    if (user == null || !user.getPwd().equals(pwd)) {
	        throw new RuntimeException("Credenciales incorrectas");
	    }
	    return user;
	}

	
	public void delete(String email) {
	    Optional<User> userOptional = userRepository.findByEmail(email); // Cambiar findById a findByEmail
	    if (!userOptional.isPresent()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
	    }
	    userRepository.delete(userOptional.get()); // Eliminar usando la entidad directamente
	}


	public void save(User user) {
	    userRepository.save(user); // Este método usa el repositorio JPA para guardar los cambios
	}

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = userRepository.findByToken(token); // Devuelve un Optional<User>
        
        if (!optionalUser.isPresent()) {
            return false; // Token no válido
        }

        User user = optionalUser.get(); // Extrae el usuario del Optional
        user.setPwd(newPassword); // Cambia la contraseña
        user.setToken(null); // Invalida el token
        userRepository.save(user); // Guarda el usuario actualizado en la base de datos
        return true;
    }

	public synchronized void clearOld() {
		long time = System.currentTimeMillis();
		for (User user : this.users.values())
			if (time> 600_000 + user.getCreationTime())
				this.delete(user.getEmail());
	}
	
	public User findByEmail(String email) {
	    return userRepository.findByEmail(email).orElse(null);
	}

	
	public void markAsPaid(String email) {
	    Optional<User> userOptional = userRepository.findByEmail(email); // Cambiar findById a findByEmail
	    if (userOptional.isPresent()) {
	        User user = userOptional.get();
	        user.setIsPaid(true);
	        userRepository.save(user);
	    } else {
	        throw new RuntimeException("Usuario no encontrado");
	    }
	}


    public boolean isTokenExpired(User user) {
        if (user.getTokenExpiry() == null) {
            return true; // Si no hay fecha de expiración, asumimos que está expirado
        }
        return System.currentTimeMillis() > user.getTokenExpiry();
    }
    public User findByToken(String token) {
        System.out.println("Buscando token: " + token); // Log para verificar el token
        Optional<User> optionalUser = userRepository.findByToken(token);
        if (!optionalUser.isPresent()) {
            System.out.println("Token no encontrado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado.");
        }
        return optionalUser.get();
    }





}














