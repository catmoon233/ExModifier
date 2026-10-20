package net.exmo.exmodifier.pack;

import java.util.List;

public record PackData(String name, String namespace, List<PackDocument> documents) {}
