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

.panel-block {
    justify-content: normal !important;
    display: block;
}

.level-right {
    justify-content: flex-end !important;
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

                    <nav class="level">
                        <!-- Left side -->
                        <div class="level-left">
                            <span class="panel-icon">
<i class="fas fa-file-alt" aria-hidden="true"></i>
</span> {{file}}
                        </div>

                        <!-- Right side -->
                        <div class="level-right">
                            <a class="button is-danger" v-on:click.stop="deleteFile(file)">
                                <span class="icon is-small">
      <i class="fas fa-trash-alt"></i>
    </span>
                            </a>
                        </div>
                    </nav>
                </a>
                <div class="panel-block">
                  <nav class="level">
                      <!-- Left side -->
                      <div class="file">
                          <label class="file-label">
                              <input class="file-input" type="file" id="file" ref="file">
                              <span class="file-cta">
      <span class="file-icon">
        <i class="fas fa-upload"></i>
      </span>
                              <span class="file-label">
        Upload file
      </span>
                              </span>
                          </label>
                      </div>

                      <!-- Right side -->
                      <div class="level-right">
                        <a class="button is-primary" v-on:click="refresh()">
  <span class="icon">
    <i class="fas fa-sync-alt"></i>
  </span>
  <span>Refresh</span>
</a>
                      </div>
                  </nav>
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
            file: '',
            file2: '',
            status: 0,
            socket: {},
            chart: {},
            current_file: '',
            data: {
                datasets: [{
                    label: '',
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
      refresh() {
        this.files = [];
        this.status = 0;
        this.socket.send("Files");
      },
        process(name) {
                this.data.datasets[0].label = name;
                this.socket.status = 1;
                this.socket.send("Data");
                this.socket.send(name);
                this.data.datasets[0].data = [];
            },
            deleteFile(file) {
                this.files = [];
                this.status = 0;
                this.socket.send("Delete");
                this.socket.send(file);
            },
            handleFileUpload() {
                this.files = [];
                this.status = 0;
                this.file = this.$refs.file.files[0];
                this.socket.send("Add");
                this.socket.send(this.file.name);
                this.socket.send(this.file2);

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
                    // legend: {
                    //     display: false
                    // },
                    scales: {
                        yAxes: [{
                            ticks: {}
                        }],
                        xAxes: [{
                            type: 'linear',
                            ticks: {}
                        }]
                    }
                }
            });
            this.chart = myChart;

            document.getElementById('file')
                .addEventListener('change', getFile)
            var that = this;

            function getFile(event) {
                const input = event.target
                if ('files' in input && input.files.length > 0) {
                    placeFileContent(
                        document.getElementById('content-target'),
                        input.files[0])
                }
            }

            function placeFileContent(target, file) {
                readFileContent(file).then(content => {
                    that.file2 = content;
                    that.handleFileUpload();
                }).catch(error => console.log(error))
            }

            function readFileContent(file) {
                const reader = new FileReader()
                return new Promise((resolve, reject) => {
                    reader.onload = event => resolve(event.target.result)
                    reader.onerror = error => reject(error)
                    reader.readAsText(file)
                })
            }

            var ws = new WebSocket('ws://localhost:8000/socket');
            var that = this;
            ws.onopen = function() {
                console.log("connection opened");
                that.socket = ws;
            };
            // Status:
            // 0 - Transfer list of files
            // 1 - Transfer data of chart


            ws.onmessage = function(evt) {
                switch (that.status) {
                    case 0:
                        if (evt.data == "All files sent") {
                            console.log("files loaded");
                            that.status = 1;
                        } else {
                            that.files.push(evt.data)
                        }
                        break;
                    case 1:
                        if (evt.data == "All data sent") {
                            that.chart.update();

                        } else {
                            var split = evt.data.split(/\s{1,}/);
                            // console.log(split[0], split[1]);
                            that.data.datasets[0].data.push({
                                x: split[0],
                                y: split[1]
                            });
                        }
                        break;
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
