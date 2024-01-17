package fr.dudie.nominatim.client;

/*
 * [license]
 * Nominatim Java API client
 * ~~~~
 * Copyright (C) 2010 - 2014 Dudie
 * ~~~~
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * [/license]
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;


import com.google.gson.Gson;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 * Parses a json response from the Nominatim API for a reverse geocoding request.
 * 
 * @author Jérémie Huchet
 */
public final class NominatimResponseHandler<T> implements HttpClientResponseHandler<T> {

    /** Gson instance for Nominatim API calls. */
    private final Gson gsonInstance;

    /** The expected type of objects. */
    private final Type responseType;

    /**
     * Constructor.
     * 
     * @param gsonInstance
     *            the Gson instance
     * @param responseType
     *            the expected type of objects
     */
    public NominatimResponseHandler(final Gson gsonInstance, final Type responseType) {

        this.gsonInstance = gsonInstance;
        this.responseType = responseType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.hc.core5.http.io.HttpClientResponseHandler#handleResponse(org.apache.hc.core5.http.ClassicHttpResponse)
     */
    @Override
    public T handleResponse(final ClassicHttpResponse response) throws IOException {

        InputStream content = null;
        final T addresses;

        try {
            final int status = response.getCode();
            if (status >= HttpStatus.SC_BAD_REQUEST) {
                throw new IOException(String.format("HTTP error: %s %s", status, response.getReasonPhrase()));
            }
            content = response.getEntity().getContent();
            addresses = gsonInstance
                    .fromJson(new InputStreamReader(content, StandardCharsets.UTF_8), responseType);
        } finally {
            if (null != content) {
                content.close();
            }
            EntityUtils.consume(response.getEntity());
        }

        return addresses;
    }
}
