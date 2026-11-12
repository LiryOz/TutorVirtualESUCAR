package cl.duoc.tutorvirtual.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.tutorvirtual.TutorApp
import cl.duoc.tutorvirtual.data.model.CuentasDemo
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository
import cl.duoc.tutorvirtual.nativo.GestorFotos
import cl.duoc.tutorvirtual.validation.LoginValidator
import cl.duoc.tutorvirtual.validation.ResultadoLogin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val rol: Rol = Rol.ESTUDIANTE,
    val usuario: String = "",
    val clave: String = "",
    val errorUsuario: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,
    val intentosFallidos: Int = 0,      // cambia en cada error: la tarjeta "se sacude"
    val ingresando: Boolean = false
)

/**
 * ViewModel del inicio de sesión. Al ingresar guarda la sesión en DataStore;
 * MainActivity observa ese dato y cambia sola a la app principal.
 */
class LoginViewModel(private val preferencias: PreferenciasRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onRolChange(rol: Rol) = _estado.update { it.copy(rol = rol, errorGeneral = null) }

    fun onUsuarioChange(v: String) = _estado.update {
        it.copy(usuario = v, errorUsuario = null, errorGeneral = null)
    }

    fun onClaveChange(v: String) = _estado.update {
        it.copy(clave = v, errorClave = null, errorGeneral = null)
    }

    /** Rellena el formulario con la cuenta genérica del perfil elegido. */
    fun usarCuentaDemo() = _estado.update {
        val cuenta = CuentasDemo.deRol(it.rol)
        it.copy(usuario = cuenta.usuario, clave = cuenta.clave, errorUsuario = null, errorClave = null, errorGeneral = null)
    }

    fun ingresar() {
        val s = _estado.value
        if (s.ingresando) return
        viewModelScope.launch {
            _estado.update { it.copy(ingresando = true, errorGeneral = null) }
            delay(700) // pequeña espera para que se note la animación de "cargando"
            when (val r = LoginValidator.intentar(s.usuario, s.clave, s.rol)) {
                is ResultadoLogin.Exito -> {
                    val fotoVieja = preferencias.iniciarSesion(r.cuenta)
                    GestorFotos.eliminar(fotoVieja)
                    // Este ViewModel vive en la Activity y sigue existiendo tras "Cerrar sesión":
                    // se deja el formulario limpio (sin contraseña y con el botón activo)
                    // para el próximo ingreso. Sin esto, el botón quedaba "pegado" cargando.
                    _estado.value = LoginUiState(rol = s.rol, usuario = s.usuario.trim())
                }
                is ResultadoLogin.CamposInvalidos -> fallar(errorUsuario = r.usuario, errorClave = r.clave)
                ResultadoLogin.CredencialesIncorrectas -> fallar(general = "Usuario o contraseña incorrectos")
                is ResultadoLogin.PerfilDistinto -> fallar(
                    general = "Esta cuenta es de perfil ${r.rolDeLaCuenta.etiqueta}. Cámbialo arriba para ingresar."
                )
            }
        }
    }

    private fun fallar(general: String? = null, errorUsuario: String? = null, errorClave: String? = null) =
        _estado.update {
            it.copy(
                ingresando = false,
                errorGeneral = general,
                errorUsuario = errorUsuario,
                errorClave = errorClave,
                intentosFallidos = it.intentosFallidos + 1
            )
        }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TutorApp
                LoginViewModel(app.contenedor.preferencias)
            }
        }
    }
}
