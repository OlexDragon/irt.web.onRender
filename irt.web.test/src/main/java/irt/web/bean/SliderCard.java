package irt.web.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import irt.web.bean.jpa.WebContent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class SliderCard {

	private String headerId;
	private String headerText;

	private String bodyId;
	private String bodyText;

	private String linkId;
	private String linkHref;

	public static Map<Integer, SliderCard> fieldsToCards(List<WebContent> cardFields){

		final Map<Integer, SliderCard> cards = new HashMap<>();

		cardFields.forEach(
				cf->{

					final String nodeId = cf.getNodeId();
					final int id = Integer.parseInt(nodeId.replaceAll("\\D", ""));

					final SliderCard card = Optional.ofNullable(cards.get(id))

							.orElseGet(()->{
								final SliderCard sliderCard = new SliderCard();
								cards.put(id, sliderCard);
								return sliderCard;
							});

					if(nodeId.startsWith("sliderTitle")) {

						card.headerId = cf.getNodeId();
						card.headerText = cf.getValue();

					}else if(nodeId.startsWith("sliderDescription")) {

						card.bodyId = cf.getNodeId();
						card.bodyText = cf.getValue();

					}else if(nodeId.startsWith("sliderLink")) {

						card.linkId = cf.getNodeId();
						card.linkHref = cf.getValue();
					}
				});

		return cards;
	}
}
