package pt.ulisboa.tecnico.sec.filesystem.replication;

public final class AttackFlag {
	private static boolean _tamperingFlag = false;
	private static boolean _impersonationFlag = false;
	
	public static boolean isBeingTampered() {
		return _tamperingFlag;
	}
	
	public static boolean isBeingImpersonated() {
		return _impersonationFlag;
	}
	
	public static void activateTamperingFlag() {
		_tamperingFlag = true;
	}
	
	public static void activateImpersonationFlag() {
		_impersonationFlag = true;
	}
	
	public static void deactivateTamperingFlag() {
		_tamperingFlag = false;
	}
	
	public static void deactivateImpersonationFlag() {
		_impersonationFlag = false;
	}
}
