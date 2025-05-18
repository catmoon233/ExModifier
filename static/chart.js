// 图表样式配置
const chartTheme = {
    fontColor: '#fff',
    gridColor: '#404040',
    colors: {
        cpu: '#ff6384',
        memory: '#36a2eb',
        tps: '#4bc0c0',
        disk: '#ff9f40'
    }
};

// 初始化资源图表
const initChart = (ctx, type) => {
    return new Chart(ctx, {
        type: 'line',
        plugins: [ChartZoom],
        data: {
            labels: [],
            datasets: [{
                label: 'CPU使用率 (%)',
                data: [],
                borderColor: chartTheme.colors.cpu,
                backgroundColor: chartTheme.colors.cpu + '33',
                fill: true,
                tension: 0.2,
                pointRadius: 2
            }, {
                label: '内存使用 (MB)',
                data: [],
                borderColor: chartTheme.colors.memory,
                backgroundColor: chartTheme.colors.memory + '33',
                fill: true,
                tension: 0.2,
                pointRadius: 2
            }, {
                label: '存储使用 (GB)',
                data: [],
                borderColor: chartTheme.colors.disk,
                backgroundColor: chartTheme.colors.disk + '33',
                fill: true,
                tension: 0.2,
                pointRadius: 2,
                hidden: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: chartTheme.fontColor }
                },
                zoom: {
                    zoom: {
                        wheel: { enabled: true },
                        pinch: { enabled: true },
                        mode: 'x'
                    },
                    pan: {
                        enabled: true,
                        mode: 'x'
                    }
                }
            },
            scales: {
                x: {
                    ticks: { color: chartTheme.fontColor },
                    grid: { color: chartTheme.gridColor }
                },
                y: {
                    ticks: { color: chartTheme.fontColor },
                    grid: { color: chartTheme.gridColor },
                    beginAtZero: true
                }
            }
        }
    });
};

// 初始化TPS图表
const initTpsChart = (ctx) => {
    return new Chart(ctx, {
        type: 'line',
        plugins: [ChartZoom],
        data: {
            labels: [],
            datasets: [{
                label: '服务器TPS',
                data: [],
                borderColor: chartTheme.colors.tps,
                backgroundColor: chartTheme.colors.tps + '33',
                fill: true,
                tension: 0.2,
                pointRadius: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { labels: { color: chartTheme.fontColor } },
                zoom: {
                    zoom: { wheel: { enabled: true }, mode: 'x' },
                    pan: { enabled: true, mode: 'x' }
                }
            },
            scales: {
                x: {
                    ticks: { color: chartTheme.fontColor },
                    grid: { color: chartTheme.gridColor }
                },
                y: {
                    min: 0,
                    max: 20,
                    ticks: { color: chartTheme.fontColor },
                    grid: { color: chartTheme.gridColor }
                }
            }
        }
    });
};

// 时间格式化函数
const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;
};

// 数据更新逻辑
let lastUpdate = Date.now();
const updateData = async () => {
    try {
        const now = Date.now();
        const response = await fetch('/api/serverinfo');
        if (!response.ok) throw new Error(`HTTP错误 ${response.status}`);
        const data = await response.json();

        // 更新基础指标
        document.getElementById('entityCount').textContent = data.entityCount;
        document.getElementById('playerCount').textContent = data.onlinePlayers;
        document.getElementById('playerListCount').textContent = data.onlinePlayers;
        document.getElementById('loadedChunks').textContent = data.loadedChunks;
        document.getElementById('serverPing').textContent = `${data.ping}ms`;
        document.getElementById('diskUsage').textContent = `${(data.diskUsage / 1024).toFixed(1)}GB`;

        // 更新进度条
        document.getElementById('entityProgress').style.width =
            Math.min(data.entityCount/5000*100, 100) + '%';
        document.getElementById('playerProgress').style.width =
            Math.min(data.onlinePlayers/20*100, 100) + '%';

        // 更新TPS相关显示
        document.getElementById('tpsValue').textContent = data.tps.toFixed(1);
        document.getElementById('tpsProgress').style.width =
            Math.min((data.tps/20)*100, 100) + '%';

        // 更新世界时间
        const gameTime = data.worldTime % 24000;
        const hours = Math.floor(gameTime / 1000 + 6) % 24;
        const minutes = Math.floor((gameTime % 1000) / 1000 * 60);
        document.getElementById('worldTime').textContent =
            `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;

        // 更新图表数据
        const timeLabel = formatTime(now);
        updateChartData(resourceChart, timeLabel, [
            data.cpuUsage,
            data.memoryUsage,
            data.diskUsage
        ]);

        updateChartData(tpsChart, timeLabel, [data.tps]);

        // 更新玩家列表
        const playersContainer = document.getElementById('players');
        playersContainer.innerHTML = data.players.map(player => `
            <div class="list-group-item">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h5 class="mb-1">${player.name}</h5>
                        <small class="text-muted">${player.dimension}</small>
                    </div>
                    <div class="text-end">
                        <small class="d-block">${player.ping}ms</small>
                        <small class="text-success">${player.health.toFixed(1)}❤️</small>
                    </div>
                </div>
                <div class="mt-2">
                    <div class="d-flex justify-content-between small">
                        <span>位置：X${player.position.x} Y${player.position.y} Z${player.position.z}</span>
                    </div>
                    <div class="equipment-grid mt-2">
                        ${player.equipment.map(item => `
                            <div class="equipment-item" data-bs-toggle="tooltip" 
                                 title="${item.name}\n耐久：${item.durability}/${item.maxDurability}">
                                <i class="item-${item.id}"></i>
                            </div>
                        `).join('')}
                    </div>
                </div>
            </div>
        `).join('');

        // 初始化工具提示
        new bootstrap.Tooltip(document.body, {
            selector: '[data-bs-toggle="tooltip"]'
        });

    } catch (error) {
        console.error('数据更新失败:', error);
        document.getElementById('serverHealth').className = 'badge bg-danger';
        document.getElementById('serverHealth').textContent = '连接失败';
    }
};

// 图表切换功能
document.querySelectorAll('[data-chart]').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('[data-chart]').forEach(b => b.classList.remove('active'));
        this.classList.add('active');

        const chartType = this.dataset.chart;
        document.getElementById('resourceChart').classList.toggle('d-none', chartType !== 'resource');
        document.getElementById('tpsChart').classList.toggle('d-none', chartType !== 'tps');
    });
});

// 初始化图表
const resourceCtx = document.getElementById('resourceChart').getContext('2d');
const resourceChart = initChart(resourceCtx);
const tpsCtx = document.getElementById('tpsChart').getContext('2d');
const tpsChart = initTpsChart(tpsCtx);

// 定时更新
setInterval(updateData, 2000);
updateData();