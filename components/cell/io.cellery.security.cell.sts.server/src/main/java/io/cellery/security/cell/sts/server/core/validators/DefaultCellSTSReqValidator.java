/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.cellery.security.cell.sts.server.core.validators;

import io.cellery.security.cell.sts.server.core.CelleryCellSTSServer;
import io.cellery.security.cell.sts.server.core.Constants;
import io.cellery.security.cell.sts.server.core.exception.CellSTSRequestValidationFailedException;
import io.cellery.security.cell.sts.server.core.model.CellStsRequest;
import io.cellery.security.cell.sts.server.core.model.config.CellStsConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of cell STS request validator.
 */
public class DefaultCellSTSReqValidator implements CellSTSRequestValidator {

    private static final Logger log = LoggerFactory.getLogger(CelleryCellSTSServer.class);

    public DefaultCellSTSReqValidator() {

    }

    @Override
    public void validate(CellStsRequest cellStsRequest) throws CellSTSRequestValidationFailedException {

        String subject = cellStsRequest.getRequestHeaders().get(Constants.CELLERY_AUTH_SUBJECT_HEADER);
        if (StringUtils.isNotBlank(subject)) {
            throw new CellSTSRequestValidationFailedException("A subject header is found in the inbound request," +
                    " before security validation: " + subject);
        }
    }

    @Override
    public boolean isAuthenticationRequired(CellStsRequest cellStsRequest) throws
            CellSTSRequestValidationFailedException {

        String path = cellStsRequest.getRequestContext().getPath();
        Optional<String> unProtectedResult = CellStsConfiguration.getInstance().getUnsecuredAPIS().stream().
                filter(unProtectedPath -> match(path, unProtectedPath)).findAny();
        log.info("Validating isAuthenticaitonRequered for context: {}", path);
        log.info(CellStsConfiguration.getInstance().getUnsecuredAPIS().toString());
        if (unProtectedResult.isPresent()) {
            log.info("Unprotected resource match found. Hence returning false");
            return false;
        }
        return true;
    }

    private static boolean match(String url, String regex) {

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        return m.find();
    }

}
