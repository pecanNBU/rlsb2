package com.hzgc.kafka.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.kafka.dynamicrepo.util.SerializerUtils;
import kafka.serializer.Decoder;
import kafka.utils.VerifiableProperties;

/**
 * Created by Administrator on 2017-7-28.
 */
public class SearchOptionDecoder implements Decoder<SearchOption> {
    public SearchOptionDecoder(VerifiableProperties verifiableProperties) {
    }

    @Override
    public SearchOption fromBytes(byte[] bytes) {
        return SerializerUtils.BytesToObject(bytes);
    }
}