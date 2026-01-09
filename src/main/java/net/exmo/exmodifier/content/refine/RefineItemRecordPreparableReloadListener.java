package net.exmo.exmodifier.content.refine;

import net.exmo.exmodifier.util.AbstractReloadListener;

import static net.exmo.exmodifier.util.ExUtil.classToString;

public class RefineItemRecordPreparableReloadListener extends AbstractReloadListener<RefineItemRecord> {

    public RefineItemRecordPreparableReloadListener() {
        super("refine_item_record",
            "loading default item refine item record data...",
            RefineItemRecord.SERIALIZE::fromJson,
            (key, data) -> {
                if (data.getItem() != null) {
                    RefineHandle.refineItemRecords.put(data.getItem(), data);
                }
            });
    }

    @Override
    public String getName() {
        return classToString(this.getClass());
    }
}