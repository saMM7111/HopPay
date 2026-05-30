package com.demo.hoppay.model;

/**
 * A single, human-readable event in a traced payment journey across the mesh.
 *
 * @param stage  short title for the step (e.g. "Signature verified")
 * @param detail supporting detail (e.g. hop count, balances, failure reason)
 * @param status one of OK, INFO, FAIL — drives styling in the dashboard timeline
 */
public record FlowStep(String stage, String detail, String status) {
	public static FlowStep ok(String stage, String detail) {
		return new FlowStep(stage, detail, "OK");
	}

	public static FlowStep info(String stage, String detail) {
		return new FlowStep(stage, detail, "INFO");
	}

	public static FlowStep fail(String stage, String detail) {
		return new FlowStep(stage, detail, "FAIL");
	}
}
