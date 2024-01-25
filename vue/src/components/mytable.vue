<script type="module" setup>

import { reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'

const textarea1 = ref('')
const tableHeight = ref(100)
function setTableHeight(){
    tableHeight.value = window.innerHeight - 100
}
setTableHeight()
addEventListener('resize', setTableHeight)

// const data = reactive([{"AD":"中国 广东省湛江市赤坎区 中国电信","IP":"0:0:0:0:0:0:0:1","IT":"2022-08-16-15-43-43_1","MI":"http://localhost:8080/jn/Python/xbc/01.png","N":1,"TM":1703863901044,"TT":"《五行星学编程0.5.1》Python使用说明书（1）","U":"http://localhost:8080/jn/Python/xbc/01.jsp","UA":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0","ip":"0:0:0:0:0:0:0:1"}]);
// 流量监控数据
const data = reactive([]);
// 正在加载流量监控
const dataloading = ref(true);
// 读cookie获取wsid
for (let i of document.cookie.split(/;[ ]*/g)) {
    if (i.startsWith('wsid=')) {
        var wsid = i.slice('wsid='.length)
        break
    }
}
// 标准构建用于webSocket的URL
const webSocketURL = new URL(location.origin)
webSocketURL.protocol = location.protocol.replace('http', 'ws')
webSocketURL.pathname = /^\/(.+\/)?/.exec(location.pathname)[0] + './wscapi'
webSocketURL.searchParams.set('wsid', wsid)
console.log(webSocketURL);
// 建立webSocket连接
var webSocket = new WebSocket(webSocketURL);
// 添加webSocket事件触发器
webSocket.onmessage = (message) => {
     console.log(message);
    // 如果数据是JSON格式
    //if (/^[[{]/.test(message.data)) {
        console.log('webSocket onmessage data')
        // JSON转对象
        const datajson = JSON.parse(message.data)
        // 逐个添加对象到data
        for (let i of datajson) {
            //i.displayTime = new Date(i.TM).toLocaleString('zh-CN')
            // data.push(i)
            data.unshift(i)
            // 如果data长度大于1000，删除第0项
            if (data.length > 1000) {
                // data.splice(0, 1)
                data.pop()
            }
        }
        console.log(data)
    //}
}
webSocket.onclose = () => {
    console.log('webSocket close');
    ElMessageBox.confirm(
        '需要重新登录吗？',
        'webSocket close',
        {
        confirmButtonText: '是',
        cancelButtonText: '否',
        // type: 'warning',
        }
    ).then(() => {
        location = './login.html'
    }).catch(() => {})

}
webSocket.onerror = (ev) => {
    console.error(ev);
    ElMessageBox.alert(String(ev), 'webSocket error', {
        confirmButtonText: '知道了',
    })
}
webSocket.onopen = () => {
    console.log('webSocket open');
    dataloading.value = false
}


function sendMyMsg(){
    console.log("sendMyMsg");
    webSocket.send(textarea1.value)
}

</script>

<template>
    <!-- <table>
        <thead>
            <tr>
                <th>序号</th>
                <th>时间</th>
                <th>IP</th>
                <th>所属地域与网络供应商</th>
                <th>项目ID</th>
                <th>项目主图</th>
                <th>项目名称</th>
                <th>项目URL</th>
                <th>UA</th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="(item) in data" :key="item.N">
                <td>{{ item.N }}</td>
                <td>{{ new Date(item.TM).toLocaleString() }}</td>
                <td>{{ item.IP }}</td>
                <td>{{ item.AD }}</td>
                <td>{{ item.IT }}</td>
                <td>{{ item.MI }}</td>
                <td>{{ item.TT }}</td>
                <td>{{ item.U }}</td>
                <td>{{ item.UA }}</td>
            </tr>
        </tbody>
    </table> -->

    <el-input
        v-model="textarea1"
        autosize
        type="textarea"
        placeholder="输入"
    />
    <el-button type="primary" @click="sendMyMsg()">发送</el-button>
    <el-table :data="data" stripe border :height=tableHeight style="width: 100%" v-loading="dataloading">
        <el-table-column prop="msg" label="消息" />
    </el-table>
</template>

