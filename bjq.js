document.addEventListener('DOMContentLoaded', function() {
    // 初始化
    initEditor();
});

function initEditor() {
    const jsonData = {
        "type": "ARMOR",
        "Example": {
            "weight": 2,
            "OnlyItems": ["minecraft:iron_boots"],
            "isRandom": true,
            "RandomNum": 3,
            "attrGethers": {
                "minecraft:generic.armor_toughness": {
                    "value": 2,
                    "slot": "auto",
                    "isAutoEquipmentSlot": true,
                    "operation": "add"
                }
            }
        }
    };

    syncJSONToForm(jsonData);
}

function syncDataToJSON() {
    const customType = document.getElementById('customType').value;
    const entryId = document.getElementById('entryId').value;
    const onlyHasThisEntry = document.getElementById('onlyHasThisEntry').checked;
    const cantSelect = document.getElementById('cantSelect').checked;
    const isRandom = document.getElementById('isRandom').checked;
    const randomNum = document.getElementById('randomNum').value;

    const jsonData = {
        "type": customType,
        [entryId]: {
            "weight": 1,
            "OnlyHasThisEntry": onlyHasThisEntry,
            "cantSelect": cantSelect,
            "isRandom": isRandom,
            "RandomNum": parseInt(randomNum),
            "attrGethers": {}
        }
    };

    // 获取属性配置
    const attributeItems = document.querySelectorAll('.attribute-item');
    attributeItems.forEach(item => {
        const attributeId = item.querySelector('.attribute-id').value;
        const value = item.querySelector('.attribute-value').value;
        const slot = item.querySelector('.attribute-slot').value;
        const operation = item.querySelector('.attribute-operation').value;

        jsonData[entryId].attrGethers[attributeId] = {
            "value": value,
            "slot": slot,
            "operation": operation
        };
    });

    // 获取仅限物品
    const onlyItemsElements = document.querySelectorAll('#onlyItemsContainer input');
    const onlyItems = [];
    onlyItemsElements.forEach(element => {
        if (element.value) {
            onlyItems.push(element.value);
        }
    });
    if (onlyItems.length > 0) {
        jsonData[entryId].OnlyItems = onlyItems;
    }

    // 获取仅限标签
    const onlyTagsElements = document.querySelectorAll('#onlyTagsContainer input');
    const onlyTags = [];
    onlyTagsElements.forEach(element => {
        if (element.value) {
            onlyTags.push(element.value);
        }
    });
    if (onlyTags.length > 0) {
        jsonData[entryId].OnlyTags = onlyTags;
    }

    document.getElementById('jsonOutput').value = JSON.stringify(jsonData, null, 2);
}

function syncFormFromJSON() {
    const jsonText = document.getElementById('jsonOutput').value;
    try {
        const jsonData = JSON.parse(jsonText);
        syncJSONToForm(jsonData);
    } catch (e) {
        alert('JSON格式错误: ' + e.message);
    }
}

function syncJSONToForm(jsonData) {
    // 处理词条类型
    if (jsonData.type) {
        document.getElementById('customType').value = jsonData.type;
    }

    // 处理词条ID
    const entryId = Object.keys(jsonData).find(key => key !== 'type');
    if (entryId) {
        document.getElementById('entryId').value = entryId;
    }

    // 处理其他字段
    if (entryId && jsonData[entryId]) {
        document.getElementById('onlyHasThisEntry').checked = jsonData[entryId].OnlyHasThisEntry || false;
        document.getElementById('cantSelect').checked = jsonData[entryId].cantSelect || false;
        document.getElementById('isRandom').checked = jsonData[entryId].isRandom !== false;
        document.getElementById('randomNum').value = jsonData[entryId].RandomNum || 1;

        // 处理仅限物品
        const onlyItemsContainer = document.getElementById('onlyItemsContainer');
        onlyItemsContainer.innerHTML = '';
        if (jsonData[entryId].OnlyItems) {
            jsonData[entryId].OnlyItems.forEach(item => {
                addItemToContainer(onlyItemsContainer, item);
            });
        }

        // 处理仅限标签
        const onlyTagsContainer = document.getElementById('onlyTagsContainer');
        onlyTagsContainer.innerHTML = '';
        if (jsonData[entryId].OnlyTags) {
            jsonData[entryId].OnlyTags.forEach(tag => {
                addTagToContainer(onlyTagsContainer, tag);
            });
        }

        // 处理属性配置
        const attributesContainer = document.getElementById('attributesContainer');
        attributesContainer.innerHTML = '';
        if (jsonData[entryId].attrGethers) {
            Object.keys(jsonData[entryId].attrGethers).forEach(attributeId => {
                const attribute = jsonData[entryId].attrGethers[attributeId];
                addAttributeToContainer(attributesContainer, attributeId, attribute.value, attribute.slot, attribute.operation);
            });
        }
    }
}

function addItem(containerId, value = '') {
    const container = document.getElementById(containerId);
    addItemToContainer(container, value);
}

function addItemToContainer(container, value) {
    const item = document.createElement('div');
    item.className = 'array-item';
    item.innerHTML = `
        <input type="text" value="${value}" placeholder="物品ID">
        <button class="delete-btn" onclick="removeItem(this)">✕</button>
    `;
    container.appendChild(item);
}

function addTag(containerId, value = '') {
    const container = document.getElementById(containerId);
    addTagToContainer(container, value);
}

function addTagToContainer(container, value) {
    const item = document.createElement('div');
    item.className = 'array-item';
    item.innerHTML = `
        <input type="text" value="${value}" placeholder="标签ID">
        <button class="delete-btn" onclick="removeItem(this)">✕</button>
    `;
    container.appendChild(item);
}

function addAttribute() {
    const container = document.getElementById('attributesContainer');
    addAttributeToContainer(container);
}

function addAttributeToContainer(container, attributeId = '', value = '', slot = 'auto', operation = 'add') {
    const item = document.createElement('div');
    item.className = 'attribute-item';
    item.innerHTML = `
        <input type="text" class="attribute-id" value="${attributeId}" placeholder="属性ID">
        <input type="text" class="attribute-value" value="${value}" placeholder="数值">
        <select class="attribute-operation">
            <option value="add" ${operation === 'add' ? 'selected' : ''}>add</option>
            <option value="multiply" ${operation === 'multiply' ? 'selected' : ''}>multiply</option>
            <option value="multiply_base" ${operation === 'multiply_base' ? 'selected' : ''}>multiply_base</option>
            <option value="multiply_total" ${operation === 'multiply_total' ? 'selected' : ''}>multiply_total</option>
            <option value="mainhand" ${operation === 'mainhand' ? 'selected' : ''}>mainhand</option>
            <option value="offhand" ${operation === 'offhand' ? 'selected' : ''}>offhand</option>
            <option value="head" ${operation === 'head' ? 'selected' : ''}>head</option>
            <option value="chest" ${operation === 'chest' ? 'selected' : ''}>chest</option>
            <option value="legs" ${operation === 'legs' ? 'selected' : ''}>legs</option>
            <option value="feet" ${operation === 'feet' ? 'selected' : ''}>feet</option>
        </select>
        <select class="attribute-slot">
            <option value="auto" ${slot === 'auto' ? 'selected' : ''}>auto</option>
            <option value="head" ${slot === 'head' ? 'selected' : ''}>head</option>
            <option value="chest" ${slot === 'chest' ? 'selected' : ''}>chest</option>
            <option value="legs" ${slot === 'legs' ? 'selected' : ''}>legs</option>
            <option value="feet" ${slot === 'feet' ? 'selected' : ''}>feet</option>
            <option value="mainhand" ${slot === 'mainhand' ? 'selected' : ''}>mainhand</option>
            <option value="offhand" ${slot === 'offhand' ? 'selected' : ''}>offhand</option>
        </select>
        <button class="delete-btn" onclick="removeAttribute(this)">✕</button>
    `;

    container.appendChild(item);
}

function removeItem(button) {
    button.parentElement.remove();
    debouncedSync();
}

function removeAttribute(button) {
    button.parentElement.remove();
    debouncedSync();
}

function toggleRandomNum() {
    const isRandom = document.getElementById('isRandom').checked;
    const randomNumInput = document.getElementById('randomNum');
    randomNumInput.disabled = !isRandom;
    debouncedSync();
}

function copyJSON() {
    const jsonText = document.getElementById('jsonOutput').value;
    navigator.clipboard.writeText(jsonText).then(() => {
        alert('JSON已复制到剪贴板');
    });
}

function formatJSON() {
    const jsonText = document.getElementById('jsonOutput').value;
    try {
        const jsonData = JSON.parse(jsonText);
        const formattedJSON = JSON.stringify(jsonData, null, 2);
        document.getElementById('jsonOutput').value = formattedJSON;
    } catch (e) {
        alert('JSON格式错误: ' + e.message);
    }
}

function debouncedSync() {
    clearTimeout(window.syncTimeout);
    window.syncTimeout = setTimeout(syncDataToJSON, 300);
}