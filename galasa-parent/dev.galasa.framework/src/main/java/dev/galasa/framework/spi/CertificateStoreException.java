package dev.galasa.framework.spi;

public class CertificateStoreException extends FrameworkException {
   private static final long serialVersionUID = 1L;

   public CertificateStoreException() {
   }

   public CertificateStoreException(String message) {
       super(message);
   }

   public CertificateStoreException(Throwable cause) {
       super(cause);
   }

   public CertificateStoreException(String message, Throwable cause) {
       super(message, cause);
   }

   public CertificateStoreException(String message, Throwable cause, boolean enableSuppression,
           boolean writableStackTrace) {
       super(message, cause, enableSuppression, writableStackTrace);
   }

}
