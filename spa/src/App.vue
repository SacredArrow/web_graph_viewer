<style>

#app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
}

h1,
h2 {
    font-weight: normal;
}

ul {
    list-style-type: none;
    padding: 0;
}

li {
    display: inline-block;
    margin: 0 10px;
}

a {
    color: #42b983;
}


/*canvas{

  width:2000px !important;
  height:2000px !important;

}*/

</style>

<template>

<div>
    <link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.5.0/css/all.css" integrity="sha384-B4dIYHKNBt8Bc12p+WXckhzcICo0wtJAoU8YZTY5qE0Id1GSseTk6S+L3BlXeVIU" crossorigin="anonymous">
    <div class="columns">
        <div class="column">
            <nav class="panel">
                <p class="panel-heading">
                    Files
                </p>
                <a class="panel-block is-active" v-for="file in files" v-on:click="process(file)">
                    <span class="panel-icon">
      <i class="fas fa-file-alt" aria-hidden="true"></i>
    </span> {{file}}
                </a>
                <div class="panel-block">
                    <button class="button is-link is-outlined is-fullwidth" v-on:click="process()">
                        reset all filters
                    </button>
                </div>
            </nav>
        </div>
        <div class="column">
            <div class="chart-container" style="position: relative">
                <canvas id="myChart"></canvas>
            </div>
        </div>
    </div>
</div>

</template>

<script>

import Chart from 'chart.js'
export default {
    name: 'app',
    data() {
        return {
            files: [],
            status: 0,
            socket: {},
            chart: {},
            data: {
                datasets: [{
                    data: [],
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.2)',
                        'rgba(54, 162, 235, 0.2)',
                        'rgba(255, 206, 86, 0.2)',
                        'rgba(75, 192, 192, 0.2)',
                        'rgba(153, 102, 255, 0.2)',
                        'rgba(255, 159, 64, 0.2)'
                    ],
                    borderColor: "#c45850",
                    borderWidth: 1,
                    fill: false,
                    lineTension: 0
                }]
            }
        }
    },

    methods: {
        process(name) {
            this.socket.send(name);
            this.data.datasets[0].data = [];

        }


    },
    mounted: function() {
        this.$nextTick(function() {
            var ctx = document.getElementById("myChart");
            let myChart = new Chart(ctx, {
                type: 'line',
                data: this.data,
                options: {
                    responsive: true,
                    // maintainAspectRatio: false,
                    scales: {
                        yAxes: [{
                            ticks: {
                            }
                        }],
                        xAxes: [{
                            type: 'linear',
                            ticks: {
                            }
                        }]
                    }
                }
            });
            this.chart = myChart;

            var ws = new WebSocket('ws://localhost:8000/socket');
            var that = this;
            ws.onopen = function() {
                // Web Socket is connected, send data using send()
                // ws.send('Message to send');
                // alert("Message is sent...");
                console.log("connection opened");
                that.socket = ws;
            };
            ws.onmessage = function(evt) {
                if (that.status == 0) {
                    if (evt.data == "All files sent") {
                        console.log("files loaded");
                        that.status = 1;
                    } else {
                        that.files.push(evt.data)
                    }
                } else if (that.status == 1) {
                    if (evt.data == "All data sent") {
                        that.chart.update();

                    } else {
                        var split = evt.data.split(" ");
                        that.data.datasets[0].data.push({
                            x: split[0],
                            y: split[1]
                        });
                    }
                }
            };
            ws.onclose = function() {
                // websocket is closed.
                alert("Connection is closed...");
            };

        })
    }
}

</script>
