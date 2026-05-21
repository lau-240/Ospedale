/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

/**
 *
 * @author Laura
 */
import model.User;

public class LoginController {
    private DataStore dataStore;

    public LoginController() {
        this.dataStore = DataStore.getInstance();
    }

    public Response login(String username, String password) {
        if (username == null || username.isBlank()) {
            return new Response(400, "El nombre de usuario no puede estar vacío");
        }
        if (password == null || password.isBlank()) {
            return new Response(400, "La contraseña no puede estar vacía");
        }

        User user = dataStore.findUserByUsername(username);

        if (user == null) {
            return new Response(404, "Usuario no encontrado");
        }

        if (!user.getPassword().equals(password)) {
            return new Response(401, "Contraseña incorrecta");
        }

        return new Response(200, "Login exitoso", user.getId());
    }
}