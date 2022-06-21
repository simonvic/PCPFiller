package it.simonvic.pcpfiller.parts;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author simonvic
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public non-sealed class Memory extends PCPart {

	public static String getCSVHeader() {
		return """
        "brand","model","moduleType","speedMHz","modulesNumber","moduleSizeGB","pricePerGBEuro","color","firstWordLatency","casTiming","errorCorrection","priceEuro"
         """;
	}

	@Override
	public String toCSV() {
		return """
        "%s","%s","%s",%f,%d,%f,%f,"%s",%f,%f,%b,%f""".formatted(
			brand, model, moduleType, speedMHz,
			modulesNumber, moduleSizeGB, pricePerGBEuro, color,
			firstWordLatency, casTiming, errorCorrection, priceEuro);
	}

	public enum ModuleType {
		DDR, DDR2, DDR3, DDR4
	}

	private String brand;
	private String model;
	private ModuleType moduleType;
	private Double speedMHz;
	private Integer modulesNumber;
	private Double moduleSizeGB;
	private Double pricePerGBEuro;
	private String color;
	private Double firstWordLatency;
	private Double casTiming;
	private Boolean errorCorrection;
	private Double priceEuro;

	public Memory(Memory.JSON json) {
		this.brand = json.brand;
		this.model = json.model;
		this.moduleType = json.module_type == null ? null : ModuleType.valueOf(json.module_type);
		if (json.speed != null) {
			this.speedMHz = json.speed.cycles / 1000000d;
		}
		this.modulesNumber = json.number_of_modules;
		if (json.module_size != null) {
			this.moduleSizeGB = json.module_size.total / 1000000000d;
		}

		double jsonPricePerGB = Double.valueOf(json.price_per_gb[1]);
		this.pricePerGBEuro = jsonPricePerGB == 0 ? null : jsonPricePerGB;

		this.color = json.color;
		this.firstWordLatency = json.first_word_latency;
		this.casTiming = json.cas_timing;
		this.errorCorrection = !json.error_correction.contains("Non-ECC");

		double jsonPrice = Double.valueOf(json.price[1]);
		this.priceEuro = jsonPrice == 0 ? null : jsonPrice;
	}

	public static class JSON extends PCPart.JSON {
		
		@Override
		public PCPart build() {
			return new Memory(this);
		}

		public static class Speed {

			long cycles;
		}

		public static class ModuleSize {

			long total;
		}

		private String brand;
		private String model;
		private String module_type;
		private Speed speed;
		private int number_of_modules;
		private ModuleSize module_size;
		private String[] price_per_gb;
		private String color;
		private double first_word_latency;
		private double cas_timing;
		private String error_correction;
		private String[] price;

		public Memory.JSON brand(String brand) {
			this.brand = brand;
			return this;
		}

		public Memory.JSON model(String model) {
			this.model = model;
			return this;
		}

		public Memory.JSON moduleType(String moduleType) {
			this.module_type = moduleType;
			return this;
		}

		public Memory.JSON speed(Speed speed) {
			this.speed = speed;
			return this;
		}

		public Memory.JSON modulesNumber(int modulesNumber) {
			this.number_of_modules = modulesNumber;
			return this;
		}

		public Memory.JSON modulesSize(ModuleSize moduleSize) {
			this.module_size = moduleSize;
			return this;
		}

		public Memory.JSON pricePerGB(String[] pricePerGB) {
			this.price_per_gb = pricePerGB;
			return this;
		}

		public Memory.JSON color(String color) {
			this.color = color;
			return this;
		}

		public Memory.JSON firstWordLatency(double firstWordLatency) {
			this.first_word_latency = firstWordLatency;
			return this;
		}

		public Memory.JSON casTiming(double casTiming) {
			this.cas_timing = casTiming;
			return this;
		}

		public Memory.JSON errorCorrection(String errorCorrection) {
			this.error_correction = errorCorrection;
			return this;
		}

		public Memory.JSON priceEuro(String[] price) {
			this.price = price;
			return this;
		}

	}

}
