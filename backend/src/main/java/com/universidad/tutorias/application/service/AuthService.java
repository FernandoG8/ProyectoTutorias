package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.auth.*;
import com.universidad.tutorias.domain.entity.Usuario;

public interface AuthService {

    AuthTokensResult login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    AuthTokensResult refresh(String refreshToken);

    void logout(String refreshToken);

    UserInfoResponse buildUserInfo(Usuario usuario);
}
