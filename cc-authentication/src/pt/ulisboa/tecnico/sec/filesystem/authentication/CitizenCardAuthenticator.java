package pt.ulisboa.tecnico.sec.filesystem.authentication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.PKIXRevocationChecker.Option;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_Certif;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PTEID_TokenInfo;
import pteidlib.PteidException;
import pteidlib.pteid;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class CitizenCardAuthenticator implements IAuthenticator {

	private PublicKey _publicKey = null;
	private X509Certificate cert;
	private java.util.Base64.Encoder encoder;
	private PKCS11 pkcs11;
	private long p11_session;
	private CK_MECHANISM mechanism;
	private long signatureKey;
	private boolean verbose= false; 

	@Override
	public PublicKey getPublicKey() {
		return _publicKey;
	}

	public CitizenCardAuthenticator() {
		// TODO Auto-generated constructor stub

		try {

			if(verbose)
				System.out.println("            //Load the PTEidlibj");

			System.loadLibrary("pteidlibj");
			pteid.Init(""); // Initializes the eID Lib
			pteid.SetSODChecking(false); // Don't check the integrity of the ID,
											// address and photo (!)

			// TODO
			pteid.GetCVCRoot();

			
			String osName = System.getProperty("os.name");
			String javaVersion = System.getProperty("java.version");
			
			if(verbose)
			System.out.println("Java version: " + javaVersion);

			java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();

			String libName = "libpteidpkcs11.so";

			// access the ID and Address data via the pteidlib
			
			if(verbose){
			System.out
					.println("            -- accessing the ID  data via the pteidlib interface");

			showInfo();
			}

			X509Certificate cert = getCertFromByteArray(getCertificateInBytes(0));
			
			if(verbose)
			System.out.println("Citized Authentication Certificate " + cert);
			
			// Certificate Validation
						CertificateFactory cf = CertificateFactory.getInstance("X.509");
						
						//FileInputStream in = new FileInputStream(new File("EC_de_Autenticacao_do_Cartao_de_Cidadao_0006.cer"));
						FileInputStream in = new FileInputStream(new File("EC de Autenticacao do Cartao de Cidadao 0009.cer"));
						Certificate trust = cf.generateCertificate(in);

						/* Construct a CertPathBuilder */
						TrustAnchor anchor = new TrustAnchor((X509Certificate) trust, null);
						Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
						trustAnchors.add(anchor);

						X509CertSelector certSelector = new X509CertSelector();
						certSelector.setCertificate(cert);

						PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, certSelector);
						CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");

						/* Enable usage of revocation lists */
						PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
						rc.setOptions(EnumSet.of(Option.PREFER_CRLS));
						params.addCertPathChecker(rc);

						CertPathBuilderResult cpbr = cpb.build(params);
						if(verbose)
							System.out.println("CertPathBuilderResult" + cpbr);

						System.out.println("****************************");

						/* Now Validate the Certificate Path */

						CertPath cp = cpbr.getCertPath();
						CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
						CertPathValidatorResult cpvr = cpv.validate(cp, params);

						/*
						 * If no exception is generated here, it means that validation was
						 * successful
						 */
						System.out.println("Validation successful");


			_publicKey = cert.getPublicKey();

			// System.out.println(key.getEncoded().length);
			cert.checkValidity();

			// access the ID and Address data via the pteidlib
			if(verbose)
			System.out.println("            -- generating signature via the PKCS11 interface");

			if (-1 != osName.indexOf("Windows"))
				libName = "pteidpkcs11.dll";
			else if (-1 != osName.indexOf("Mac"))
				libName = "pteidpkcs11.dylib";
			Class pkcs11Class = Class
					.forName("sun.security.pkcs11.wrapper.PKCS11");
			if (javaVersion.startsWith("1.5.")) {
				Method getInstanceMethode = pkcs11Class.getDeclaredMethod(
						"getInstance", new Class[] { String.class,
								CK_C_INITIALIZE_ARGS.class, boolean.class });
				pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] {
						libName, null, false });
			} else {
				Method getInstanceMethode = pkcs11Class.getDeclaredMethod(
						"getInstance", new Class[] { String.class,
								String.class, CK_C_INITIALIZE_ARGS.class,
								boolean.class });
				pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] {
						libName, "C_GetFunctionList", null, false });
			}

			// Open the PKCS11 session
			if(verbose)
			System.out.println("            //Open the PKCS11 session");
			p11_session = pkcs11.C_OpenSession(0,
					PKCS11Constants.CKF_SERIAL_SESSION, null, null);

			// Token login
			if(verbose)
			System.out.println("            //Token login");
			pkcs11.C_Login(p11_session, 1, null);
			CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);

			// Get available keys
			if(verbose)
			System.out.println("            //Get available keys");
			CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
			attributes[0] = new CK_ATTRIBUTE();
			attributes[0].type = PKCS11Constants.CKA_CLASS;
			attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

			pkcs11.C_FindObjectsInit(p11_session, attributes);
			long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);

			// points to auth_key
			if(verbose)
			System.out.println("            //points to auth_key. No. of keys:"
					+ keyHandles.length);

			signatureKey = keyHandles[0]; // test with other keys to see
												// what you get
			pkcs11.C_FindObjectsFinal(p11_session);

			// initialize the signature method
			if(verbose)
			System.out.println("            //initialize the signature method");
			mechanism = new CK_MECHANISM();
			mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
			mechanism.pParameter = null;
			

		} catch (Throwable e) {
			System.out.println("[Catch] Exception: " + e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public byte[] signData(byte[] data) {
		// sign
		if(verbose)
		System.out.println("            //sign data");
		byte[] signature = null;
		
		try {
			pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
			signature = pkcs11.C_Sign(p11_session, data);
		} catch (PKCS11Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("            //signature:"
//				+ encoder.encode(signature));
     	return signature;
	}

	@Override
	public boolean verifySignature(byte[] in_signature, byte[] data) {
		Signature pksignature;
		try {
			pksignature = Signature.getInstance("SHA1withRSA");
		
		pksignature.initVerify(_publicKey);
		pksignature.update(data);
		return pksignature.verify(in_signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException	 e) {
			e.printStackTrace();
		} 
		return false;
	
	}

	public static void showInfo() {
		try {

			int cardtype = pteid.GetCardType();
			switch (cardtype) {
			case pteid.CARD_TYPE_IAS07:
				System.out.println("IAS 0.7 card\n");
				break;
			case pteid.CARD_TYPE_IAS101:
				System.out.println("IAS 1.0.1 card\n");
				break;
			case pteid.CARD_TYPE_ERR:
				System.out.println("Unable to get the card type\n");
				break;
			default:
				System.out.println("Unknown card type\n");
			}

			// Read ID Data
			PTEID_ID idData = pteid.GetID();
			if (null != idData)
				PrintIDData(idData);

			// Read Addr Data
			PTEID_ADDR addrData = pteid.GetAddr();
			if (null != addrData)
				PrintAddrData(addrData);

			// Read Picture Data
			PTEID_PIC picData = pteid.GetPic();
			if (null != picData) {
				String photo = "photo.jp2";
				FileOutputStream oFile = new FileOutputStream(photo);
				oFile.write(picData.picture);
				oFile.close();
				System.out.println("Created " + photo);
			}

			// Read Pins
			PTEID_Pin[] pins = pteid.GetPINs();

			// Read TokenInfo
			PTEID_TokenInfo token = pteid.GetTokenInfo();

			// Read personal Data
			byte[] filein = { 0x3F, 0x00, 0x5F, 0x00, (byte) 0xEF, 0x07 };
			byte[] file = pteid.ReadFile(filein, (byte) 0x81);

		} catch (PteidException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void PrintIDData(PTEID_ID idData) {
		System.out.println("DeliveryEntity : " + idData.deliveryEntity);
		System.out.println("PAN : " + idData.cardNumberPAN);
		System.out.println("...");
	}

	private static void PrintAddrData(PTEID_ADDR addrData) {
		System.out.println("District : " + addrData.district);
		// System.out.println("PAN : " + addrData.cardNumberPAN);
		System.out.println("...");
	}

	// Returns the CITIZEN AUTHENTICATION CERTIFICATE
	public byte[] getCitizenAuthCertInBytes() {
		return getCertificateInBytes(0); // certificado 0 no Cartao do Cidadao
											// eh o de autenticacao
	}

	// Returns the n-th certificate, starting from 0
	private  byte[] getCertificateInBytes(int n) {
		byte[] certificate_bytes = null;
		try {
			PTEID_Certif[] certs = pteid.GetCertificates();
			if(verbose)
				System.out.println("Number of certs found: " + certs.length);
			int i = 0;
			
			if(verbose)
			for (PTEID_Certif cert : certs) {
				System.out
						.println("-------------------------------\nCertificate #"
								+ (i++));
				System.out.println(cert.certifLabel);
			}

			certificate_bytes = certs[n].certif; // gets the byte[] with the
													// n-th certif

			// pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a
			// eID Lib
		} catch (PteidException e) {
			e.printStackTrace();
		}
		return certificate_bytes;
	}

	public static X509Certificate getCertFromByteArray(byte[] certificateEncoded)
			throws CertificateException {
		CertificateFactory f = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificateEncoded);
		X509Certificate cert = (X509Certificate) f.generateCertificate(in);
		return cert;
	}

	@Override
	public void exit() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>EXIT<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		try {
			pkcs11.C_CloseSession(p11_session);
			pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD);
		} catch (PteidException | PKCS11Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // OBRIGATORIO Termina a
													// eID Lib
		
	}

}
