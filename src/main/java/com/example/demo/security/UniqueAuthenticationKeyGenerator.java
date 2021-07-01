package com.example.demo.security;

/*
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Basic key generator taking into account the client id, scope, resource ids and username (principal name) if they
 * exist.
 */
public class UniqueAuthenticationKeyGenerator implements AuthenticationKeyGenerator {

    private static final String CLIENT_ID = "idClient";

    private static final String SCOPE = "read write";

    private static final String USERNAME = "root";

    private static final String UUID_KEY = "uuid";

    

    public String extractKey(OAuth2Authentication authentication) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        OAuth2Request authorizationRequest = authentication.getOAuth2Request();
        if (!authentication.isClientOnly()) {
            values.put(USERNAME, authentication.getName());
        }
        values.put(CLIENT_ID, authorizationRequest.getClientId());
        if (authorizationRequest.getScope() != null) {
            values.put(SCOPE, OAuth2Utils.formatParameterList(authorizationRequest.getScope()));
        }
        Map<String, Serializable> extentions = authorizationRequest.getExtensions();
        String uuid = null;
        if (extentions == null) {
            extentions = new HashMap<String, Serializable>(1);
            uuid = UUID.randomUUID().toString();
            extentions.put(UUID_KEY, uuid);
        } else {
            uuid = (String) extentions.get(UUID_KEY);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
                extentions.put(UUID_KEY, uuid);
            }
        }
        values.put(UUID_KEY, uuid);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(values.toString().getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }
}