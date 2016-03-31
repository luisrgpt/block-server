package pt.ulisboa.tecnico.sec.filesystem;

final class AttackFlag {
	private static boolean _tamperingFlag = false;
	private static boolean _impersonationFlag = false;
	
	static boolean isBeingTampered() {
		return _tamperingFlag;
	}
	
	static boolean isBeingImpersonated() {
		return _impersonationFlag;
	}
	
	static void activateTamperingFlag() {
		_tamperingFlag = true;
	}
	
	static void activateImpersonationFlag() {
		_impersonationFlag = true;
	}
	
	static void deactivateTamperingFlag() {
		_tamperingFlag = false;
	}
	
	static void deactivateImpersonationFlag() {
		_impersonationFlag = false;
	}
}
