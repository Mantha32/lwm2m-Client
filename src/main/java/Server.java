import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.leshan.Link;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;

import java.util.Collection;

public class Server {

    public static void main (String[] args) {

        LeshanServerBuilder builder = new LeshanServerBuilder();
        NetworkConfig networkConfig = LeshanServerBuilder.createDefaultNetworkConfig();
        networkConfig.set(NetworkConfig.Keys.PROTOCOL_STAGE_THREAD_COUNT, 5);
        builder.setCoapConfig(networkConfig);
        final LeshanServer server = builder.build();
        server.start();

        server.getRegistrationService().addListener(new RegistrationListener() {

            public void registered(Registration registration, Registration previousReg,
                                   Collection<Observation> previousObsersations) {
                System.out.println("new device: " + registration.getEndpoint());
                try {
                    //System.out.println("Supported Devices: ");
                    for (Link l: registration.getObjectLinks()
                         ) {
                        System.out.println(l);
                    }
                    ObserveRequest request = new ObserveRequest(6, 0, 5);
                    ObserveResponse cResponse = server.send(registration, request, 5000);
                    System.out.println(cResponse);
                    if (cResponse.isSuccess()) {
                        System.out.println("Device Date:" + ((LwM2mResource)cResponse.getContent()).getValue());
                    }else {
                        System.out.println("Failed to read:" + cResponse.getCode() + " " + cResponse.getErrorMessage());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {

                    System.out.println("device is still here: " + updatedReg.getEndpoint());

            }

            public void unregistered(Registration registration, Collection<Observation> observations, boolean expired,
                                     Registration newReg) {
                System.out.println("device left: " + registration.getEndpoint());
            }
        });


    }


}
