package fr.free.nrw.commons.upload;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import com.icafe4j.image.meta.*;
import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataReader;
import com.icafe4j.image.meta.iptc.IPTC;
import com.icafe4j.string.StringUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.icafe4j.image.meta.Metadata;
import com.icafe4j.image.meta.MetadataType;
import com.icafe4j.image.meta.iptc.IPTC;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import retrofit2.http.Url;
import timber.log.Timber;

public class ReadFBMD {
    public static Single<Integer> processMetadata(String filePath) throws IOException {
        Map<MetadataType, Metadata> metadataMap = Metadata.readMetadata(filePath);
        IPTC iptc = (IPTC)metadataMap.get(MetadataType.IPTC);
        Timber.d("time_lag_2"+String.valueOf(System.currentTimeMillis()));

        if(iptc != null) {
            for (String key: iptc.getDataSets().keySet()){
                //study the data
            }
        }
        return Single.just(1);
    }
    private static void printMetadata(MetadataEntry entry, String indent, String increment) {
        //logger.info(indent + entry.getKey() (StringUtils.isNullOrEmpty(entry.getValue())? "" : ": " + entry.getValue()));
        if(entry.isMetadataEntryGroup()) {
            indent += increment;
            Collection<MetadataEntry> entries = entry.getMetadataEntries();
            for(MetadataEntry e : entries) {
                printMetadata(e, indent, increment);
            }
        }
    }

}

