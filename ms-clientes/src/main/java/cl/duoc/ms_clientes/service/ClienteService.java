package cl.duoc.ms_clientes.service;

import cl.duoc.ms_clientes.dto.ClienteResponse;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    public ClienteResponse buscarPorId(Long id) {
        return new ClienteResponse(
            id,
            "Dante Rodriguez",
            "dant.rodriguez@duocuc.cl"
        );
    }
}