package com.metiz.pelconnect.network;

import org.json.JSONObject;

/**
 * Created by espl on 27/9/16.
 */
public interface VolleyStringBack {
    /**
     * On success.
     *
     * @param result the result
     */
    // on success result
    void onSuccess(String result);

    void onError(String result);

}
