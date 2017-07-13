import React from 'react';
import PropTypes from 'prop-types';
import {
  Text,
  StyleSheet,
} from 'react-native';
import murmurHash from 'murmur2js';

const propTypes = {
  id: PropTypes.number,
  ...Text.propTypes,
};

const contextTypes = {
  nativeNavigationInstanceId: PropTypes.string,
};

const PARAGRAPHS = [
  'Leo hendrerit. Nec ac ullamcorper sem egestas, mi diamlorem convallis magna, vitae eget, in' +
  ' suspendisse massa esse eget, adipiscing penatibus tincidunt augue urna interdum. Nunc aliquam' +
  ' ut nulla parturient suspendisse, ut tristique velit orci ipsum nunc, velit consectetur in' +
  ' feugiat pellentesque blandit. Nec eget vitae, ipsum mi mi rhoncus mauris eu, egestas semper' +
  ' dolor et, dolor sit quisque amet aptent pretium at. Varius consequat et posuere parturient' +
  ' in tristique, phasellus elit molestie consectetuer sapien suscipit quam.',
  'In suscipit aliquam eu, nam est torquent, convallis mauris condimentum luctus, est diam felis.' +
  ' Ipsum suspendisse, ullamcorper fusce suspendisse ac cursus, dis consectetuer tellus blandit,' +
  ' cras malesuada vestibulum ipsum, at sit duis a mi. Dui mi arcu ipsum ultricies, vel arcu.' +
  ' Dolor cursus in lorem, ut vitae vulputate mus. Suspendisse hendrerit felis felis pede arcu' +
  ' eu, vivamus mi suscipit. Tincidunt dui vel pretium pretium. Pellentesque integer, vestibulum' +
  ' posuere, adipiscing non nunc tristique condimentum ipsum. Sem luctus lorem neque ac lacus,' +
  ' consectetuer fermentum facilisi elit eu eget lorem, sit cras rutrum etiam sed.',
  'Aliquam velit, placerat morbi. Ipsum orci et at mauris volutpat, mattis accumsan velit mauris' +
  ' posuere sodales suscipit, leo elit ut suspendisse, felis risus potenti fringilla,' +
  ' reprehenderit sit leo adipiscing quam in. In ligula, rutrum mauris, tincidunt sed ut diam' +
  ' amet lacus mollis. Vivamus eu diam et diam eu quisque, adipiscing vitae suscipit, rhoncus' +
  ' eros nam, nec sed dictumst est ultricies neque posuere, nec ridiculus nullam diam vestibulum' +
  ' ac elit. Vitae duis sagittis pede posuere, dolor mi sed est, porttitor orci, nulla' +
  ' suspendisse urna vestibulum aliquam, eget id vitae suscipit hymenaeos amet quis. Neque id' +
  ' vel aenean ipsum, velit porttitor magna, aliquam fames nunc venenatis.',
  'Placerat luctus rutrum interdum dictum, vitae platea dapibus suspendisse et mattis, lacinia' +
  ' facilisis et sit, elit nisl quam dolor. Aliquet voluptas at convallis, quis diam at, neque' +
  ' justo adipiscing in a amet varius, accumsan erat. Elit feugiat ipsum gravida, dui metus sit' +
  ' donec nam ut, rutrum magnis gravida minima nisl, dictum eget dolor vel condimentum non. Est' +
  ' convallis nulla ligula metus lorem, dis adipiscing cras enim. Neque augue amet, morbi elit' +
  ' nulla per sit, pretium nec venenatis ipsum nonummy purus. Purus dui ut scelerisque, ac' +
  ' assumenda facilisi malesuada. Risus sit luctus volutpat magna, arcu ipsum sapien potenti' +
  ' etiam nonummy ante, orci quis augue ultricies amet duis dui, ac vulputate. Nunc vel rutrum.' +
  ' Purus habitant taciti. Rutrum nascetur mattis suspendisse, tellus auctor vel lorem' +
  ' vestibulum, porttitor sem dictumst vestibulum vehicula phasellus. Urna eu mauris dui.',
  'Rutrum et sed id adipiscing, posuere unde mollis ut in faucibus. Ante facilisis quis' +
  ' consequuntur, scelerisque tincidunt felis felis, mauris quam elit donec lectus, in lobortis' +
  ' mauris lacinia convallis, in varius et a esse habitasse magna. Molestie dis turpis in dolor,' +
  ' bibendum a sem felis, tincidunt varius eleifend convallis, morbi impedit, dictum maecenas' +
  ' turpis. Orci risus arcu at velit quis, dui ante elit mauris purus, fringilla lacus, ac' +
  ' mollis, nunc auctor. Quam libero pretium lectus, aliquet suspendisse lacus sed, cras' +
  ' condimentum libero rhoncus quam, aliquam sociis faucibus non nonummy felis orci, morbi' +
  ' vulputate pellentesque hendrerit mauris justo. Eu nunc ipsum interdum nulla ut, sed ipsum' +
  ' ipsum sagittis erat, morbi velit massa in odio aliquam. Mauris interdum sed harum porttitor' +
  ' interdum in. Donec in eu, nascetur non quis nec ultricies, purus wisi nunc.',
  'Temporibus ut urna hendrerit. Sed mi turpis elit vitae id ut, voluptatem pede malesuada,' +
  ' purus pretium, orci sit accumsan morbi sit, eleifend in. Eu sed varius et sapien ipsum,' +
  ' metus lorem pretium mauris, mi nam ut consectetuer, consequat dolorem ipsum. Nihil bibendum' +
  ' volutpat arcu ac consectetuer ac, nec nulla molestie accumsan mauris dui, tellus sed' +
  ' integer quis ipsum. Leo nec eros, sem viverra risus, ad vel soluta turpis, nulla etiam.' +
  ' Rutrum ante eros augue vulputate tristique, blandit nibh, augue wisi vivamus eget' +
  ' elementum est. Maecenas nonummy, eleifend velit. Mattis parturient nullam a dolor eget.' +
  ' Volutpat id, lacus donec. Lobortis lorem maecenas ligula, ut quia, pellentesque accumsan' +
  ' semper ut turpis urna, placerat massa luctus, lacus vivamus.',
  'Facilisis in porttitor, eros nulla consectetuer ridiculus felis pede lobortis, neque id' +
  ' senectus, maecenas urna aliquam rutrum tortor pretium, vitae eu lacus sem eget nibh neque.' +
  ' Tristique justo in sed odio pellentesque suspendisse, vivamus nec massa quis posuere' +
  ' rutrum, malesuada in in ut eu similique. Metus eget, amet tempus metus nam porttitor, nam' +
  ' ac pede fringilla et phasellus, pharetra sem posuere. Nec quam erat scelerisque, duis' +
  ' pretium ornare dolor tellus velit accumsan. A praesent egestas consectetuer luctus morbi' +
  ' nostra, ex tristique pharetra, nostrum fermentum viverra, urna habitasse mi vitae vivamus' +
  ' ligula, elit erat eget eu pellentesque at nulla. Quis lacinia interdum modi. Venenatis' +
  ' sint arcu alias in vehicula malesuada, eleifend massa in id purus nulla, voluptatem nullam,' +
  ' sodales mauris nec et. Molestie adipiscing imperdiet praesent nisl ut, in lectus lacus' +
  ' egestas, id lectus et dictum a nunc.',
  'Purus nam morbi cursus vehicula non, nonummy elit, morbi quis orci nibh. Egestas lorem ut' +
  ' tempus blandit vitae neque. Montes nam amet mi augue ante cras, lacinia hac varius sodales' +
  ' integer, proin imperdiet, suspendisse non, justo laboriosam. Dolor magna, sed id sit, vel' +
  ' ullamcorper ullamcorper. Orci at, consectetuer leo adipiscing magna, eu maecenas vitae,' +
  ' ullamcorper donec vitae lorem suspendisse cras, massa venenatis.',
  'Leo nullam blandit, magna sociosqu augue porttitor quis iaculis massa, lectus ut nonummy' +
  ' per justo blandit. Varius ut phasellus rhoncus, nam vitae porta accumsan, maecenas' +
  ' facilisis tempor orci. Faucibus penatibus curabitur sed nunc, pellentesque id ligula' +
  ' nullam, sed metus, metus condimentum dolor lacus, sed urna ut amet nibh porttitor pede.' +
  ' Nibh curabitur faucibus dui, egestas ullamcorper condimentum. Lectus neque per vulputate' +
  ' arcu, semper arcu aliquet risus wisi non est. Pulvinar imperdiet et et eget congue,' +
  ' tempor id et dignissim ligula, erat dictum quis risus eu ligula, sed tristique bibendum' +
  ' praesent tortor tincidunt pellentesque. Quisque nisl vitae ipsum purus risus.',
  'Consectetuer nibh vitae accumsan, imperdiet at vehicula ante est, ligula hac justo' +
  ' ridiculus ornare mauris, lectus parturient nunc, risus in viverra. Eget praesent egestas' +
  ' adipiscing duis, vehicula morbi lectus pharetra egestas etiam mattis, mi quis' +
  ' consectetuer tellus, nunc arcu. Nibh primis, tellus nullam velit adipiscing non turpis,' +
  ' vehicula morbi, sed molestie, ullamcorper ultrices. Elementum sollicitudin, vel pede,' +
  ' ornare etiam nibh pharetra massa cursus viverra, enim semper euismod, nunc aliquet' +
  ' bibendum. Nam libero aenean tortor dictum, rutrum in. Dictumst est nullam est. Gravida' +
  ' suspendisse fusce congue quis, integer ultrices sit vestibulum, lorem quis, in at' +
  ' malesuada tellus mauris vel. Magna et. In id mi ante, diam amet, gravida ipsum enim risus' +
  ' sed, ac sem sed id sed vel.',
];

export default class LoremParagraph extends React.Component {
  render() {
    const { id, style } = this.props;
    const { nativeNavigationInstanceId } = this.context;
    const index = (id || murmurHash(nativeNavigationInstanceId)) % PARAGRAPHS.length;
    return (
      <Text {...this.props} style={[styles.header, style]}>
        {PARAGRAPHS[index]}
      </Text>
    );
  }
}

LoremParagraph.propTypes = propTypes;
LoremParagraph.contextTypes = contextTypes;

const styles = StyleSheet.create({
  header: {

  },
});
