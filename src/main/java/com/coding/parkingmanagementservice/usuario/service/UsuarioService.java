package com.coding.parkingmanagementservice.usuario.service;

import com.coding.parkingmanagementservice.usuario.dto.CrearUsuarioRequest;
import com.coding.parkingmanagementservice.usuario.dto.EditarUsuarioRequest;
import com.coding.parkingmanagementservice.usuario.dto.UsuarioListItemResponse;
import java.util.List;

public interface UsuarioService {
    List<UsuarioListItemResponse> listarUsuarios();
    UsuarioListItemResponse crearUsuario(CrearUsuarioRequest request);
    UsuarioListItemResponse editarUsuario(Long idUsuario, EditarUsuarioRequest request);
    void eliminarUsuario(Long idUsuario);
}
