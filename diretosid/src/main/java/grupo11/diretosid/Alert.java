package grupo11.diretosid;

public class Alert {

	private final String zona;
	private final String sensor;
	private final String hora;
	private final String leitura;
	private final String tipo;
	private String cultura;
	private String mensagem;

	public Alert(String zona, String sensor, String hora, String leitura, String tipo) {
		this.zona = zona;
		this.sensor = sensor;
		this.hora = hora;
		this.leitura = leitura;
		this.tipo = tipo;
	}
	
	public Alert(String zona, String sensor, String hora, String leitura, String tipo, String cultura, String mensagem) {
		this.zona = zona;
		this.sensor = sensor;
		this.hora = hora;
		this.leitura = leitura;
		this.tipo = tipo;
		this.cultura = cultura;
		this.mensagem = mensagem;
	}

	public String getZona() {
		return zona;
	}

	public String getSensor() {
		return sensor;
	}

	public String getHora() {
		return hora;
	}

	public String getLeitura() {
		return leitura;
	}

	public String getTipo() {
		return tipo;
	}

	public String getCultura() {
		return cultura;
	}

	public void setCultura(String cultura) {
		this.cultura = cultura;
	}
	
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}
}
