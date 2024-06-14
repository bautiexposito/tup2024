package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClienteServiceTest {

    @Mock
    private ClienteDao clienteDao;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testClienteMenor18Años() {
        Cliente clienteMenorDeEdad = new Cliente();
        clienteMenorDeEdad.setFechaNacimiento(LocalDate.of(2020, 2, 7));
        assertThrows(IllegalArgumentException.class, () -> clienteService.darDeAltaCliente(clienteMenorDeEdad));
    }

    @Test
    public void testClienteSuccess() throws ClienteAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setFechaNacimiento(LocalDate.of(1978,3,25));
        cliente.setDni(29857643);
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);
        clienteService.darDeAltaCliente(cliente);

        verify(clienteDao, times(1)).save(cliente);
    }

    @Test
    public void testClienteAlreadyExistsException() throws ClienteAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456437);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(26456437, false)).thenReturn(new Cliente());

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.darDeAltaCliente(pepeRino));
    }



    @Test
    public void testAgregarCuentaAClienteSuccess() throws TipoCuentaAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());

    }


    @Test
    public void testAgregarCuentaAClienteDuplicada() throws TipoCuentaAlreadyExistsException {
        Cliente luciano = new Cliente();
        luciano.setDni(26456439);
        luciano.setNombre("Pepe");
        luciano.setApellido("Rino");
        luciano.setFechaNacimiento(LocalDate.of(1978, 3,25));
        luciano.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(luciano);

        clienteService.agregarCuenta(cuenta, luciano.getDni());

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, luciano.getDni()));
        verify(clienteDao, times(1)).save(luciano);
        assertEquals(1, luciano.getCuentas().size());
        assertEquals(luciano, cuenta.getTitular());

    }

    //Agregar una CA$ y CC$ --> success 2 cuentas, titular peperino
    @Test
    public void testAgregarCAyCC() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setDni(43046272);
        cliente.setNombre("Bautista");
        cliente.setApellido("Exposito");
        cliente.setFechaNacimiento(LocalDate.of(2000, 11, 6));
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        // Crea cuenta: CA$ (Caja de Ahorro en Pesos)
        Cuenta cuentaCA = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        // Crea cuenta: CC$ (Cuenta Corriente en Pesos)
        Cuenta cuentaCC = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);

        when(clienteDao.find(43046272, true)).thenReturn(cliente);

        // Agregar la primera cuenta (CA$) al cliente
        clienteService.agregarCuenta(cuentaCA, cliente.getDni());

        // Verifica
        verify(clienteDao, times(1)).save(cliente);
        assertEquals(1, cliente.getCuentas().size());
        assertEquals(cliente, cuentaCA.getTitular());

        // Agregar la segunda cuenta (CC$) al cliente
        clienteService.agregarCuenta(cuentaCC, cliente.getDni());

        // Verifica
        verify(clienteDao, times(2)).save(cliente);
        assertEquals(2, cliente.getCuentas().size());
        assertEquals(cliente, cuentaCC.getTitular());

        // Verificar que ambas cuentas están en la lista de cuentas del cliente
        assertTrue(cliente.getCuentas().contains(cuentaCA));
        assertTrue(cliente.getCuentas().contains(cuentaCC));
    }

    //Agregar una CA$ y CAU$S --> success 2 cuentas, titular peperino...
    @Test
    public void testAgregarCAyCAU() throws CuentaAlreadyExistsException, TipoCuentaAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setDni(43046272);
        cliente.setNombre("Bautista");
        cliente.setApellido("Exposito");
        cliente.setFechaNacimiento(LocalDate.of(2000, 11, 6));
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        // Crea cuenta: CA$ (Caja de Ahorro en Pesos)
        Cuenta cuentaCA = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        // Crea cuenta: CAU$ (Cuenta Ahorro en Dolares)
        Cuenta cuentaCAU = new Cuenta()
                .setMoneda(TipoMoneda.DOLARES)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(43046272, true)).thenReturn(cliente);

        // Agregar la primera cuenta (CA$) al cliente
        clienteService.agregarCuenta(cuentaCA, cliente.getDni());

        // Verifica
        verify(clienteDao, times(1)).save(cliente);
        assertEquals(1, cliente.getCuentas().size());
        assertEquals(cliente, cuentaCA.getTitular());

        // Agregar la segunda cuenta (CAU$) al cliente
        clienteService.agregarCuenta(cuentaCAU, cliente.getDni());

        // Verifica
        verify(clienteDao, times(2)).save(cliente);
        assertEquals(2, cliente.getCuentas().size());
        assertEquals(cliente, cuentaCAU.getTitular());

        // Verificar que ambas cuentas están en la lista de cuentas del cliente
        assertTrue(cliente.getCuentas().contains(cuentaCA));
        assertTrue(cliente.getCuentas().contains(cuentaCAU));
    }

    //Testear clienteService.buscarPorDni, caso exito
    @Test
    public void testBuscarPorDni() throws ClienteAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setDni(43046272);
        cliente.setNombre("Bautista");
        cliente.setApellido("Exposito");
        cliente.setFechaNacimiento(LocalDate.of(2000, 11, 6));
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(43046272, true)).thenReturn(cliente);

        Cliente foundCliente = clienteService.buscarClientePorDni(43046272);
        assertEquals(cliente, foundCliente);
    }

    //Testear clienteService.buscarPorDni, caso falla
    /*
    @Test
    @Disabled
    public void testBuscarPorDni_Falla() {
        when(clienteDao.find(43046272, true)).thenReturn(null);

        Cliente foundCliente = clienteService.buscarClientePorDni(43046272);
        assertNull(foundCliente);
    }
     */
}
