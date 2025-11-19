package co.edu.unipiloto.proyectoenvio.model;
public class Usuario {
    private Long id;
    private String nombre;
    private String usuario;
    private String email;
    private String password;
    private String direccion;
    private String celular;
    private String rol;
    private String fechaNacimiento;
    private String genero;
    private String fotoBase64; // coincide con backend

    // 🔥 Constructor vacío obligatorio para Retrofit/Gson
    public Usuario() {}

    // 🔥 Constructor con parámetros (útil cuando creas el usuario localmente)
    public Usuario(String nombre, String usuario, String email, String password,
                   String direccion, String celular, String rol,
                   String fechaNacimiento, String genero, String fotoBase64) {

        this.nombre = nombre;
        this.usuario = usuario;
        this.email = email;
        this.password = password;
        this.direccion = direccion;
        this.celular = celular;
        this.rol = rol;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.fotoBase64 = fotoBase64;
    }

    // getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }
}

