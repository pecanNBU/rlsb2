package com.hzgc.kafka.dynamicrepo;

import com.hzgc.dubbo.dynamicrepo.SearchOption;
import com.hzgc.kafka.dynamicrepo.util.SerializerUtils;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

/**
 * Created by Administrator on 2017-7-28.
 */
public class SearchOptionEncoder implements Encoder<SearchOption> {
    public SearchOptionEncoder(VerifiableProperties verifiableProperties) {
    }

    @Override
    public byte[] toBytes(SearchOption option) { //填写你需要传输的对象
        return SerializerUtils.ObjectToBytes(option);
    }
}
