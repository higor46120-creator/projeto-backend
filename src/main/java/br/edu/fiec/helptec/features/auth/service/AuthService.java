package br.edu.fiec.helptec.features.auth.service;

import br.edu.fiec.helptec.features.auth.models.AuthResponse;
import br.edu.fiec.helptec.features.auth.models.GoogleLoginRequest;
import br.edu.fiec.helptec.features.auth.models.LoginRequest;
import br.edu.fiec.helptec.features.auth.models.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginComGoogle(GoogleLoginRequest request);
}
