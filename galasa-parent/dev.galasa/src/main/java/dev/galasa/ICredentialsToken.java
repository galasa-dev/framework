/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

public interface ICredentialsToken extends ICredentials {

    byte[] getToken();

}
