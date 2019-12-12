package com.signalfx.springdemo;

import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.lang.String;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.signalfx.codahale.SfxMetrics;
import com.signalfx.codahale.reporter.MetricMetadata;
import com.signalfx.codahale.reporter.SignalFxReporter;
import com.signalfx.endpoint.SignalFxEndpoint;
import com.signalfx.endpoint.SignalFxReceiverEndpoint;
import com.signalfx.metrics.auth.StaticAuthToken;



@Controller
public class SignalFxHello {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String helloSignal() {

      //
      // Configura o Endpoint
      // Configuracoes tbm podem ser carregadas de properties, etc.
      //
      try {
        final String ingestStr = "https://ingest.us1.signalfx.com";
        final URL ingestUrl = new URL(ingestStr);
        final SignalFxReceiverEndpoint endpoint = 
            new SignalFxEndpoint(ingestUrl.getProtocol(), ingestUrl.getHost(), ingestUrl.getPort());
            
            final MetricRegistry metricRegistry = new MetricRegistry();
            final SignalFxReporter reporter = 
                new SignalFxReporter.Builder(metricRegistry, new StaticAuthToken("token"), ingestStr)
                    .setEndpoint(endpoint)
                    .build();
      
//
//  Setup do Reporter
//
reporter.start(1, TimeUnit.SECONDS);
final MetricMetadata metricMetadata = reporter.getMetricMetadata();
final SfxMetrics metrics = new SfxMetrics(metricRegistry, metricMetadata);

///
/// Envia uma Metrica
///

        // Enviar o timestamp atual como um Gauge
        metrics.registerGauge("sfx.test.gaugetest", new Gauge<Long>() {
            public Long getValue() {
                return System.currentTimeMillis();
            }
        });

        // Enviar métrica com Dimensão

        Long queueValue = 100L;

        metrics.registerGauge("sfx.test.tamanho_fila", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return queueValue;
            }
        }, "nome_fila", "customer_backlog");



        return ("Métricas Realtime SignalFX enviadas!!!");


    }
    catch (IOException e){
         throw new RuntimeException(e);}
    }   

}