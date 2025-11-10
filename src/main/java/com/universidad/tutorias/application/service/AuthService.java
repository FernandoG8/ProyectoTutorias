package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.domain.entity.Usuario;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    RefreshTokenResponse refresh(RefreshTokenRequest request);

    UserInfoResponse buildUserInfo(Usuario usuario);
}
