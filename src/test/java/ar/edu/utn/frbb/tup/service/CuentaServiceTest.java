package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaNoSoportadaException;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CuentaServiceTest {

    @Mock
    private CuentaDao cuentaDao;

    @Mock
    private ClienteService clienteService;

    @Spy
    @InjectMocks
    private CuentaService cuentaService;

    @BeforeEach
    public void setUp() {}

    @Test
    public void testCuentaExistente() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException, CuentaNoSoportadaException {
        Cuenta cuentaExistente = new Cuenta();
        cuentaExistente.setNumeroCuenta(11223);

        when(cuentaDao.find(11223)).thenReturn(cuentaExistente);

        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuentaExistente, 45037310));

        verify(cuentaDao, times(1)).find(11223);
    }

    @Test
    public void testCuentaNoSoportada() throws CuentaNoSoportadaException, CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.INVERSIONES);

        doReturn(null).when(cuentaDao).find(anyLong());

        assertThrows(CuentaNoSoportadaException.class, () -> {
            cuentaService.darDeAltaCuenta(cuenta, 43046272);
        });
    }

    @Test
    public void testClienteYaTieneCuentaDeEsteTipo() throws CuentaAlreadyExistsException,CuentaNoSoportadaException,TipoCuentaAlreadyExistsException{
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);

        Cliente cliente = new Cliente();
        cliente.setDni(43046272);
        cliente.setNombre("Bautista");
        cliente.setApellido("Exposito");
        cliente.addCuenta(cuenta);
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        doThrow(TipoCuentaAlreadyExistsException.class).when(clienteService).agregarCuenta(cuenta,cliente.getDni());
        assertThrows(TipoCuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, cliente.getDni()));
    }

    @Test
    public void testCuentaCreadaExitosamente() throws TipoCuentaAlreadyExistsException, CuentaAlreadyExistsException, CuentaNoSoportadaException {
        Cuenta cuenta = new Cuenta();
        cuenta.setTipoCuenta(TipoCuenta.CAJA_AHORRO);
        cuenta.setMoneda(TipoMoneda.PESOS);

        Cliente cliente = new Cliente();
        cliente.setDni(43046272);
        cliente.setNombre("Bautista");
        cliente.setApellido("Exposito");

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        cuentaService.darDeAltaCuenta(cuenta,43046272);

        verify(cuentaDao, times(1)).save(cuenta);
    }



}